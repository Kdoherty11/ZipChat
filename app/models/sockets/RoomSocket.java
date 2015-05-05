package models.sockets;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.typesafe.plugin.RedisPlugin;
import models.entities.AbstractRoom;
import models.entities.Message;
import models.entities.PublicRoom;
import models.entities.User;
import models.sockets.messages.Join;
import models.sockets.messages.Quit;
import models.sockets.messages.RosterNotification;
import models.sockets.messages.Talk;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;
import static play.libs.Json.toJson;
import static utils.DbUtils.buildEntityNotFoundString;
import static utils.DbUtils.findEntityById;
import static utils.DbUtils.findExistingEntityById;

public class RoomSocket extends UntypedActor {

    private static final String CHANNEL = "messages";

    static final ActorRef defaultRoom = Akka.system().actorOf(Props.create(RoomSocket.class));

    // Key is roomId, value is the users connected to that room
    static final Map<Long, Map<Long, WebSocket.Out<JsonNode>>> rooms = new HashMap<>();

    static final Cache<Long, User> usersCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    static final Cache<Long, PublicRoom> publicRoomsCache = CacheBuilder.newBuilder().maximumSize(1000).build();

    static final Map<Long, SocketKeepAlive> clientHeartbeats = new HashMap<>();

    static {

        //subscribe to the message channel
        Akka.system().scheduler().scheduleOnce(
                Duration.create(10, TimeUnit.MILLISECONDS),
                () -> {
                    Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();
                    try {
                        j.subscribe(new MessageListener(), CHANNEL);
                    } finally {
                        play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
                    }
                },
                Akka.system().dispatcher()
        );
    }

    public static void join(final long roomId, final long userId, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception {

        Logger.debug("User " + userId + " is joining " + roomId);
        // Join the default room. Timeout should be longer than the Redis connect timeout (2 seconds)
        String result = (String) Await.result(ask(defaultRoom, new Join(roomId, userId, out), 3000), Duration.create(3, SECONDS));

        if ("OK".equals(result)) {

            Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();
            try {
                JPA.withTransaction(() -> {

                    Object[] roomMembers = j.smembers(String.valueOf(roomId))
                            .stream()
                            .mapToLong(Long::parseLong)
                            .filter(id -> id != userId && id != SocketKeepAlive.USER_ID)
                            .mapToObj(id -> findExistingEntityById(User.class, id))
                            .toArray();

                    ObjectNode event = Json.newObject();
                    event.put("event", "roomMembers");
                    event.put("message", toJson(roomMembers));

                    out.write(event);
                });

               // out.write(toJson(roomMemberIds));
            } finally {
                play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
            }

            // For each event received on the socket,
            in.onMessage(event -> {
                Talk talk = new Talk(roomId, userId, event.get("message").asText());

                Jedis jedis = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();
                try {
                    //All messages are pushed through the pub/sub channel
                    jedis.publish(RoomSocket.CHANNEL, Json.stringify(toJson(talk)));
                } finally {
                    play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
                }
            });

            // When the socket is closed.
            in.onClose(() -> {
                // Send a Quit message to the room.
                defaultRoom.tell(new Quit(roomId, userId), null);
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
    @Transactional
    public void onReceive(Object message) throws Exception {

        Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();

        try {
            if (message instanceof Join) {
                // Received a Join message
                Join join = (Join) message;
                Logger.debug("onReceive: " + join);
                receiveJoin(j, join);
            } else if (message instanceof Quit) {
                // Received a Quit message
                Quit quit = (Quit) message;
                Logger.debug("onReceive: " + quit);
                receiveQuit(j, quit);
            } else if (message instanceof RosterNotification) {
                //Received a roster notification
                RosterNotification rosterNotify = (RosterNotification) message;
                Logger.debug("onReceive: " + rosterNotify);
                receiveRosterNotification(rosterNotify);
            } else if (message instanceof Talk) {
                // Received a Talk message
                Talk talk = (Talk) message;
                Logger.debug("onReceive: " + talk);
                receiveTalk(talk);
            } else {
                unhandled(message);
            }
        } catch (Throwable throwable) {
            Logger.error("Problem receiving message: " + throwable.getMessage());
        } finally {
            play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
        }
    }

    private void receiveTalk(Talk talk) throws Throwable {
        long roomId = talk.getRoomId();
        long userId = talk.getUserId();
        String messageText = talk.getText();

        notifyRoom(roomId, "talk", userId, messageText);

        if (userId == SocketKeepAlive.USER_ID) {
            return;
        }

        Message message = storeMessage(talk);

        User sender = message.sender;
        usersCache.put(userId, sender);

        AbstractRoom abstractRoom = message.room;
        if (abstractRoom instanceof PublicRoom) {
            PublicRoom publicRoom = (PublicRoom) abstractRoom;
            publicRoomsCache.put(roomId, publicRoom);
            NotificationUtils.messageSubscribers(publicRoom, sender, messageText);
        }
    }

    @Transactional
    private Message storeMessage(Talk talk) throws Throwable {

        if (SocketKeepAlive.USER_ID == talk.getUserId()) {
            throw new IllegalArgumentException("Trying to store a keep alive message");
        }

        return JPA.withTransaction(() -> {
            Message message = new Message(talk.getRoomId(), talk.getUserId(), talk.getText());
            message.addToRoom();
            return message;
        });
    }

    private void receiveJoin(Jedis j, Join join) {
        long roomId = join.getRoomId();
        long userId = join.getUserId();
        if (j.sismember(String.valueOf(roomId), String.valueOf(userId))) {
            getSender().tell("This userId is already used", getSelf());
            Logger.error("user: " + userId + " is trying to join room: " + roomId + " but the userId is already in use");
        } else {
            if (!rooms.containsKey(roomId)) {
                // Creating a new room
                Logger.debug("Adding new room and keep alive: " + roomId);

                rooms.put(roomId, new HashMap<>());

                SocketKeepAlive socketKeepAlive = new SocketKeepAlive(roomId, defaultRoom);
                clientHeartbeats.put(roomId, socketKeepAlive);
            }
            //Add the member to this node and the global roster
            rooms.get(roomId).put(userId, join.getChannel());
            j.sadd(String.valueOf(roomId), String.valueOf(userId));

            //Publish the join notification to all nodes
            RosterNotification rosterNotify = new RosterNotification(roomId, userId, "join");
            j.publish(RoomSocket.CHANNEL, Json.stringify(toJson(rosterNotify)));

            getSender().tell("OK", getSelf());
        }
    }

    private void receiveQuit(Jedis j, Quit quit) {
        long roomId = quit.getRoomId();
        Map<Long, WebSocket.Out<JsonNode>> members = rooms.get(roomId);

        if (members != null) {
            members.remove(quit.getUserId());
        }

        j.srem(String.valueOf(roomId), String.valueOf(quit.getUserId()));

        Set<String> roomMembers = j.smembers(String.valueOf(roomId));

        // For the robot
        if (roomMembers.size() == 1) {
            Logger.debug("Removing robot from room: " + roomId);

            rooms.remove(quit.getRoomId());

            // Remove robot
            if (clientHeartbeats.containsKey(roomId)) {
                clientHeartbeats.get(roomId).stop();
            }

        } else {
            Logger.debug("There are still members in room " + roomId + "\n" + roomMembers);

            //Publish the quit notification to all nodes
            RosterNotification rosterNotify = new RosterNotification(roomId, quit.getUserId(), "quit");
            j.publish(RoomSocket.CHANNEL, Json.stringify(toJson(rosterNotify)));
        }
    }

    private void receiveRosterNotification(RosterNotification rosterNotification) {
        if ("join".equals(rosterNotification.getDirection())) {
            notifyRoom(rosterNotification.getRoomId(), "join", rosterNotification.getUserId(), "has entered the room");
        } else if ("quit".equals(rosterNotification.getDirection())) {
            notifyRoom(rosterNotification.getRoomId(), "quit", rosterNotification.getUserId(), "has left the room");
        }
    }

    // Send a Json event to all members connected to this node
    public void notifyRoom(long roomId, String kind, long userId, String text) {
        Logger.debug("NotifyAll called with roomId: " + roomId);

        if (!rooms.containsKey(roomId)) {
            Logger.error("Not notifying rooms because rooms map does not contain room with id " + roomId);
            return;
        }

        for (WebSocket.Out<JsonNode> channel : rooms.get(roomId).values()) {

            JPA.withTransaction(() -> {
                JsonNode userJson;
                if (userId == SocketKeepAlive.USER_ID) {
                    userJson = null;
                } else {
                    User user = usersCache.getIfPresent(userId);
                    if (user != null) {
                        userJson = toJson(user);
                    } else {
                        Optional<User> userOptional = findEntityById(User.class, userId);
                        if (userOptional.isPresent()) {
                            user = userOptional.get();
                            usersCache.put(userId, user);
                            userJson = toJson(user);
                        } else {
                            Logger.error("Not notifying room because " + buildEntityNotFoundString(User.ENTITY_NAME, userId));
                            return;
                        }
                    }
                }

                ObjectNode event = Json.newObject();
                event.put("event", kind);
                event.put("user", userJson);
                event.put("message", text);

                channel.write(event);
            });
        }
    }

    // -- Messages

    public static class MessageListener extends JedisPubSub {

        @Override
        public void onMessage(String channel, String messageBody) {
            //Process messages from the pub/sub channel
            JsonNode parsedMessage = Json.parse(messageBody);

            Logger.debug("myListener onMessage: " + parsedMessage);
            Object message = null;
            String messageType = parsedMessage.get("type").asText();
            if (Talk.TYPE.equals(messageType)) {
                message = new Talk(
                        parsedMessage.get("roomId").asLong(),
                        parsedMessage.get("userId").asLong(),
                        parsedMessage.get("text").asText()
                );
            } else if (RosterNotification.TYPE.equals(messageType)) {
                message = new RosterNotification(
                        parsedMessage.get("roomId").asLong(),
                        parsedMessage.get("userId").asLong(),
                        parsedMessage.get("direction").asText()
                );
            } else if (Quit.TYPE.equals(messageType)) {
                message = new Quit(
                        parsedMessage.get("roomId").asLong(),
                        parsedMessage.get("userId").asLong()
                );
            }
            RoomSocket.remoteMessage(message);
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
        }
    }

}