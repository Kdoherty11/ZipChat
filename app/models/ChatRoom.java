package models;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.plugin.RedisPlugin;
import play.Logger;
import play.libs.Akka;
import play.libs.F;
import play.libs.Json;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ChatRoom extends UntypedActor {

    // Default room.
    static ActorRef defaultRoom = Akka.system().actorOf(Props.create(ChatRoom.class));

    private static final String CHANNEL = "messages";

    // Key is roomId, value is the users connected to that room
    Map<String, Map<String, WebSocket.Out<JsonNode>>> rooms = new HashMap<>();

    static {

        //subscribe to the message channel
        Akka.system().scheduler().scheduleOnce(
                Duration.create(10, TimeUnit.MILLISECONDS),
                new Runnable() {
                    public void run() {
                        Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();
                        j.subscribe(new MyListener(), CHANNEL);
                    }
                },
                Akka.system().dispatcher()
        );
    }


    /**
     * Join the default room.
     */
    public static void join(final String roomId, final String userId, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception {
        Logger.debug("User " + userId + " is joining " + roomId);
        // Join the default room. Timeout should be longer than the Redis connect timeout (2 seconds)
        String result = (String) Await.result(ask(defaultRoom, new Join(roomId, userId, out), 3000), Duration.create(3, SECONDS));
        Logger.debug("Got result in static join");

        if ("OK".equals(result)) {

            // For each event received on the socket,
            in.onMessage(new F.Callback<JsonNode>() {
                public void invoke(JsonNode event) {

                    Talk talk = new Talk(roomId, userId, event.get("text").asText());

                    Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();
                    try {
                        //All messages are pushed through the pub/sub channel
                        j.publish(ChatRoom.CHANNEL, Json.stringify(Json.toJson(talk)));
                    } finally {
                        play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
                    }

                }
            });

            // When the socket is closed.
            in.onClose(new F.Callback0() {
                public void invoke() {
                    // Send a Quit message to the room.
                    defaultRoom.tell(new Quit(roomId, userId), null);

                }
            });

        } else {

            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put("error", result);

            // Send the error to the socket.
            out.write(error);

        }

    }

    public static void remoteMessage(Object message) {
        defaultRoom.tell(message, null);
    }

    @Override
    public void onReceive(Object message) throws Exception {

        Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();

        try {
            if (message instanceof Join) {
                // Received a Join message
                Join join = (Join) message;

                Logger.debug("onReceive: " + join);

                // Check if this username is free.
                if (j.sismember(join.roomId, join.username)) {
                    getSender().tell("This username is already used", getSelf());
                } else {
                    if (!rooms.containsKey(join.roomId)) {
                        // Creating a new room
                        Logger.debug("Adding new room and keep alive: " + join.roomId);

                        rooms.put(join.roomId, new HashMap<>());

                        new Robot(join.roomId, defaultRoom);
                    }
                    //Add the member to this node and the global roster
                    rooms.get(join.roomId).put(join.username, join.channel);
                    j.sadd(join.roomId, join.username);

                    //Publish the join notification to all nodes
                    RosterNotification rosterNotify = new RosterNotification(join.roomId, join.username, "join");

                    Logger.debug("rosterNotify in join: " + rosterNotify);
                    Logger.debug("rosterNotify in join json: " + Json.stringify(Json.toJson(rosterNotify)));
                    Logger.debug("here... " + Json.toJson(rosterNotify));

                    j.publish(ChatRoom.CHANNEL, Json.stringify(Json.toJson(rosterNotify)));

                    getSender().tell("OK", getSelf());
                }

            } else if (message instanceof Quit) {
                // Received a Quit message
                Quit quit = (Quit) message;

                Logger.debug("onReceive: " + quit);

                //Remove the member from this node and the global roster
                rooms.get(quit.roomId).remove(quit.username);
                j.srem(quit.roomId, quit.username);

                // TODO: Check if there is nobody left in the room so we can remove the keep alives somehow

                //Publish the quit notification to all nodes
                RosterNotification rosterNotify = new RosterNotification(quit.roomId, quit.username, "quit");
                j.publish(ChatRoom.CHANNEL, Json.stringify(Json.toJson(rosterNotify)));
            } else if (message instanceof RosterNotification) {
                //Received a roster notification
                RosterNotification rosterNotify = (RosterNotification) message;

                Logger.debug("onReceive: " + rosterNotify);

                if ("join".equals(rosterNotify.direction)) {
                    notifyAll(rosterNotify.roomId, "join", rosterNotify.username, "has entered the room");
                } else if ("quit".equals(rosterNotify.direction)) {
                    notifyAll(rosterNotify.roomId, "quit", rosterNotify.username, "has left the room");
                }
            } else if (message instanceof Talk) {
                // Received a Talk message
                Talk talk = (Talk) message;
                notifyAll(talk.roomId, "talk", talk.username, talk.text);

                Logger.debug("onReceive: " + talk);

            } else {
                unhandled(message);
            }
        } finally {
            play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
        }
    }

    // Send a Json event to all members connected to this node
    public void notifyAll(String roomId, String kind, String user, String text) {
        Logger.debug("NotifyAll called with roomId: " + roomId);
        Map<String, WebSocket.Out<JsonNode>> roomMembers = rooms.get(roomId);

        if (roomMembers == null) {
            Logger.warn("Room: " + roomId + " is null");
            return;
        }

        for (WebSocket.Out<JsonNode> channel : rooms.get(roomId).values()) {

            ObjectNode event = Json.newObject();
            event.put("kind", kind);
            event.put("user", user);
            event.put("message", text);

            ArrayNode m = event.putArray("members");

            //Go to Redis to read the full roster of members. Push it down with the message.
            Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();
            try {
                for (String u : j.smembers(roomId)) {
                    m.add(u);
                }
            } finally {
                play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
            }

            channel.write(event);
        }
    }

    // -- Messages

    public static class Join {

        final String roomId;
        final String username;
        final WebSocket.Out<JsonNode> channel;

        public String getRoomId() {
            return roomId;
        }

        public String getUsername() {
            return username;
        }

        public String getType() {
            return "join";
        }

        public Join(String roomId, String username, WebSocket.Out<JsonNode> channel) {
            this.roomId = roomId;
            this.username = username;
            this.channel = channel;
        }
    }

    public static class RosterNotification {

        final String roomId;
        final String username;
        final String direction;

        public String getUsername() {
            return username;
        }

        public String getDirection() {
            return direction;
        }

        public String getType() {
            return "rosterNotify";
        }

        public String getRoomId() {
            return roomId;
        }

        public RosterNotification(String roomId, String username, String direction) {
            this.roomId = roomId;
            this.username = username;
            this.direction = direction;
        }

        @Override
        public String toString() {
            return "RosterNotification (" + roomId + ") user " + username + " is " + direction + "ing";
        }
    }

    public static class Talk {

        final String roomId;
        final String username;
        final String text;

        public String getUsername() {
            return username;
        }

        public String getText() {
            return text;
        }

        public String getType() {
            return "talk";
        }

        public String getRoomId() {
            return roomId;
        }

        public Talk(String roomId, String username, String text) {
            this.roomId = roomId;
            this.username = username;
            this.text = text;
        }

        @Override
        public String toString() {
            return "Talk (" + roomId + ") " + username + " - " + text;
        }

    }

    public static class Quit {

        final String roomId;
        final String username;

        public String getUsername() {
            return username;
        }

        public String getType() {
            return "quit";
        }

        public String getRoomId() {
            return roomId;
        }

        public Quit(String roomId, String username) {
            this.roomId = roomId;
            this.username = username;
        }

        @Override
        public String toString() {
            return username + "is quiting room: " + roomId;
        }

    }

    public static class MyListener extends JedisPubSub {
        @Override
        public void onMessage(String channel, String messageBody) {
            //Process messages from the pub/sub channel
            JsonNode parsedMessage = Json.parse(messageBody);

            Logger.debug("myListener onMessage: " + parsedMessage);
            Object message = null;
            String messageType = parsedMessage.get("type").asText();
            if ("talk".equals(messageType)) {
                message = new Talk(
                        parsedMessage.get("roomId").asText(),
                        parsedMessage.get("username").asText(),
                        parsedMessage.get("text").asText()
                );
            } else if ("rosterNotify".equals(messageType)) {
                message = new RosterNotification(
                        parsedMessage.get("roomId").asText(),
                        parsedMessage.get("username").asText(),
                        parsedMessage.get("direction").asText()
                );
            } else if ("quit".equals(messageType)) {
                message = new Quit(
                        parsedMessage.get("roomId").asText(),
                        parsedMessage.get("username").asText()
                );
            }
            ChatRoom.remoteMessage(message);
        }

        @Override
        public void onPMessage(String arg0, String arg1, String arg2) {
        }

        @Override
        public void onPSubscribe(String arg0, int arg1) {
        }

        @Override
        public void onPUnsubscribe(String arg0, int arg1) {
        }

        @Override
        public void onSubscribe(String arg0, int arg1) {
        }

        @Override
        public void onUnsubscribe(String arg0, int arg1) {
            // TODO: return resources here?
        }
    }

}