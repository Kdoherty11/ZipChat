package models.sockets;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.plugin.RedisPlugin;
import models.entities.*;
import models.sockets.events.*;
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
import utils.DbUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;
import static play.libs.Json.toJson;
import static utils.DbUtils.findExistingEntityById;

public class RoomSocket extends UntypedActor {

    private static final boolean VERBOSE = true;

    private static final String CHANNEL = "messages";

    private static final String OK_JOIN_RESULT = "OK";

    private static final String EVENT_KEY = "event";
    private static final String MESSAGE_KEY = "message";
    private static final String USER_KEY = "user";

    static final ActorRef defaultRoom = Akka.system().actorOf(Props.create(RoomSocket.class));

    // Key is roomId, value is the users connected to that room
    static final Map<Long, Map<Long, WebSocket.Out<JsonNode>>> rooms = new ConcurrentHashMap<>();

    static final Map<Long, SocketKeepAlive> clientHeartbeats = new ConcurrentHashMap<>();

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

        if (OK_JOIN_RESULT.equals(result)) {

            Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();
            try {
                JPA.withTransaction(() -> {

                    List<User> roomMembers = getRoomMembers(roomId, j);

                    ObjectNode event = Json.newObject();
                    event.put(EVENT_KEY, "joinSuccess");

                    ObjectNode message = Json.newObject();
                    message.set("roomMembers", toJson(roomMembers));

                    AbstractRoom room = findExistingEntityById(AbstractRoom.class, roomId);
                    if (room instanceof PublicRoom) {
                        boolean isSubscribed = ((PublicRoom) room).isSubscribed(userId);
                        logV("Join is subscribed: " + isSubscribed);
                        message.put("isSubscribed", isSubscribed);
                    }

                    event.set(MESSAGE_KEY, message);

                    logV("join JSON response: " + event);

                    out.write(event);
                });
            } finally {
                play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
            }

            // For each event received on the socket,
            in.onMessage(message -> {

                logV("onMessage message: " + message);

                final String event = message.get("event").asText();

                Object messageObject;
                switch (event) {
                    case Talk.TYPE:
                        if (message.has("isAnon") && message.get("isAnon").asBoolean()) {
                            messageObject = new Talk(roomId, userId, message.get("message").asText(), true);
                        } else {
                            messageObject = new Talk(roomId, userId, message.get("message").asText());
                        }
                        break;
                    case FavoriteNotification.TYPE:
                        messageObject = new FavoriteNotification(userId, Long.parseLong(message.get("messageId").asText()), message.get("action").asText());
                        break;
                    default:
                        throw new RuntimeException("Event: " + event + " is not supported");
                }

                logV("onMessage object: " + messageObject);

                Jedis jedis = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();
                try {
                    jedis.publish(RoomSocket.CHANNEL, Json.stringify(toJson(messageObject)));
                } finally {
                    play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
                }
            });

            // When the socket is closed.
            in.onClose(() -> {
                logV("Closing socket for user: " + userId);
                defaultRoom.tell(new Quit(roomId, userId), null);
            });

        } else {
            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put(EVENT_KEY, "error");
            error.put(MESSAGE_KEY, result);

            logV("Error: Could not connect: " + error);

            // Send the error to the socket.
            out.write(error);
        }

    }

    public static Optional<WebSocket.Out<JsonNode>> getWebSocket(long roomId, long userId) {
        Map<Long, WebSocket.Out<JsonNode>> room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("Can't notify user because room " + roomId + " does not exist in rooms");
        }

        return Optional.ofNullable(room.get(userId));
    }

    private static List<User> getRoomMembers(long roomId, Jedis j) {
        return getUserIdsInRoomStream(roomId, j)
                .filter(id -> id != SocketKeepAlive.USER_ID)
                .map(id -> DbUtils.findExistingEntityById(User.class, id))
                .collect(Collectors.<User>toList());
    }

    private static Set<Long> getUserIdsInRoom(long roomId, Jedis j) {
        return getUserIdsInRoomStream(roomId, j).collect(Collectors.toSet());
    }

    private static Stream<Long> getUserIdsInRoomStream(long roomId, Jedis j) {
        return j.smembers(Long.toString(roomId))
                .stream()
                .map(Long::parseLong);
    }

    public static void remoteMessage(Object message) {
        defaultRoom.tell(message, null);
    }

    @Override
    @Transactional
    public void onReceive(Object message) {
        logV("onReceive: " + message);

        Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();

        if (message instanceof Join) {
            try {
                receiveJoin(j, (Join) message);
            } catch (Exception e) {
                Logger.error("Error receiving join", e);
            }
        } else if (message instanceof Quit) {
            try {
                receiveQuit(j, (Quit) message);
            } catch (Exception e) {
                Logger.error("Error receiving quit", e);
            }
        } else if (message instanceof RosterNotification) {
            JPA.withTransaction(() -> {
                try {
                    receiveRosterNotification((RosterNotification) message);
                } catch (Exception e) {
                    Logger.error("Error receiving roster notification", e);
                }
            });
        } else if (message instanceof Talk) {
            JPA.withTransaction(() -> {
                try {
                    receiveTalk(j, (Talk) message);
                } catch (Exception e) {
                    Logger.error("Error receiving talk", e);
                }
            });
        } else if (message instanceof FavoriteNotification) {
            JPA.withTransaction(() -> {
                try {
                    receiveFavoriteNotification((FavoriteNotification) message);
                } catch (Exception e) {
                    Logger.error("Error receiving favorite notification", e);
                }
            });
        } else {
            Logger.error("Unhandled message received");
            unhandled(message);
        }

        play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
    }

    private void receiveFavoriteNotification(FavoriteNotification favoriteNotification) {
        Logger.debug("ReceiveFavoriteNotification: " + favoriteNotification);

        long messageId = favoriteNotification.getMessageId();
        Message message = findExistingEntityById(Message.class, messageId);

        long userId = favoriteNotification.getUserId();
        final User user = findExistingEntityById(User.class, userId);

        boolean success;
        if (favoriteNotification.getAction() == FavoriteNotification.Action.ADD) {
            success = message.favorite(user);
        } else {
            success = message.removeFavorite(user);
        }

        if (success) {
            notifyRoom(message.room.roomId, favoriteNotification.getAction().getType(), userId, String.valueOf(messageId));
        } else {
            ObjectNode error = Json.newObject();
            error.put(EVENT_KEY, "error");
            error.put(MESSAGE_KEY, "Problem " + favoriteNotification.getAction() + "ing a favorite");

            notifyUser(message.room.roomId, userId, error);
        }
    }


    private void receiveTalk(Jedis j, Talk talk) {
        Logger.debug("receiveTalk: " + talk);

        long roomId = talk.getRoomId();
        long userId = talk.getUserId();
        String messageText = talk.getText();

        if (userId == SocketKeepAlive.USER_ID) {
            notifyRoom(roomId, Talk.TYPE, userId, messageText);
            return;
        }

        Message message;
        try {
            message = JPA.withTransaction(() -> storeMessage(talk, j));
        } catch (Throwable throwable) {
            Logger.error("Problem storing the message", throwable);
            throw new RuntimeException("Problem storing the message: " + throwable.getMessage());
        }

        notifyRoom(roomId, Talk.TYPE, userId, Json.stringify(toJson(message)));
    }

    private void notifyUser(long roomId, long userId, JsonNode message) {
        Logger.debug("sending " + message + " to user " + userId);
        Optional<WebSocket.Out<JsonNode>> outOptional = getWebSocket(roomId, userId);

        if (outOptional.isPresent()) {
            outOptional.get().write(message);
            logV("Success notifying user");
        } else {
            Logger.error("No Out WebSocket for user " + userId + " in room " + roomId);
        }
    }

    private Message storeMessage(Talk talk, Jedis j) {
        final long senderId = talk.getUserId();
        final long roomId = talk.getRoomId();
        final boolean isAnon = talk.isAnon();
        User sender = DbUtils.findExistingEntityById(User.class, senderId);
        AbstractRoom room = DbUtils.findExistingEntityById(AbstractRoom.class, roomId);

        AbstractUser messageSender;
        if (isAnon) {
            if (room instanceof PublicRoom) {
                messageSender = AnonUser.getOrCreateAnonUser(sender, ((PublicRoom) room));
            } else {
                throw new RuntimeException("Trying to store an anon message in a private room");
            }
        } else {
            messageSender = sender;
        }

        Message message = new Message(room, messageSender, talk.getText());
        room.addMessage(message, getUserIdsInRoom(roomId, j));
        return message;
    }

    private void receiveJoin(Jedis j, Join join) {
        Logger.debug("receiveJoin: " + join);
        long roomId = join.getRoomId();
        long userId = join.getUserId();

        if (!rooms.containsKey(roomId)) {
            // Creating a new room
            Logger.debug("Adding new room " + roomId + " and adding a keep alive");
            rooms.put(roomId, new HashMap<>());
            addKeepAlive(roomId);
        }

        if (j.sismember(Long.toString(roomId), Long.toString(userId))) {
            Logger.error("User " + userId + " is trying to join room: " + roomId + " but the userId is already in use");
        } else {
            //Add the member to this node and the global roster
            j.sadd(String.valueOf(roomId), String.valueOf(userId));
        }

        rooms.get(roomId).put(userId, join.getChannel());

        //Publish the join notification to all nodes
        RosterNotification rosterNotify = new RosterNotification(roomId, userId, Join.TYPE);
        j.publish(RoomSocket.CHANNEL, Json.stringify(toJson(rosterNotify)));

        getSender().tell(OK_JOIN_RESULT, getSelf());
    }

    private void addKeepAlive(long roomId) {
        SocketKeepAlive socketKeepAlive = new SocketKeepAlive(roomId, defaultRoom);
        clientHeartbeats.put(roomId, socketKeepAlive);
    }

    private void receiveQuit(Jedis j, Quit quit) {
        Logger.debug("receiveQuit: " + quit);
        long roomId = quit.getRoomId();
        long userId = quit.getUserId();
        Map<Long, WebSocket.Out<JsonNode>> members = rooms.get(roomId);

        if (members != null) {
            members.remove(userId);
        }

        String roomIdStr = Long.toString(roomId);
        j.srem(roomIdStr, Long.toString(userId));

        Set<String> roomMembers = j.smembers(roomIdStr);

        // For the robot
        if (roomMembers.size() == 1) {

            if (roomMembers.contains(String.valueOf(SocketKeepAlive.USER_ID))) {
                Logger.debug("Removing robot from room " + roomId);

                j.srem(String.valueOf(roomId), String.valueOf(SocketKeepAlive.USER_ID));

                rooms.remove(roomId);

                // Remove robot
                if (clientHeartbeats.containsKey(roomId)) {
                    clientHeartbeats.get(roomId).stop();
                }
            } else {
                // Don't remove the room because there is still a user in it
                Logger.error("No robot in room " + roomId + " but there is a user in it");

                if (!rooms.containsKey(roomId)) {
                    Logger.error("Room " + roomId + " was never created");
                    rooms.put(roomId, new HashMap<>());
                }

                addKeepAlive(roomId);
            }
            Logger.debug("After quit room members are: " + roomMembers);
        } else {
            //Publish the quit notification to all nodes
            RosterNotification rosterNotify = new RosterNotification(roomId, userId, Quit.TYPE);
            j.publish(RoomSocket.CHANNEL, Json.stringify(toJson(rosterNotify)));
        }
    }

    private void receiveRosterNotification(RosterNotification rosterNotification) {
        Logger.debug("receiveRosterNotification: " + rosterNotification);
        if (Join.TYPE.equals(rosterNotification.getDirection())) {
            notifyRoom(rosterNotification.getRoomId(), Join.TYPE, rosterNotification.getUserId(), "has entered the room");
        } else if (Quit.TYPE.equals(rosterNotification.getDirection())) {
            notifyRoom(rosterNotification.getRoomId(), Quit.TYPE, rosterNotification.getUserId(), "has left the room");
        }
    }

    // Send a Json event to all members connected to this node
    public void notifyRoom(long roomId, String kind, long userId, String text) {
        Logger.debug("NotifyAll called with kind: " + kind + ", roomId: " + roomId + " and message: " + text);

        Map<Long, WebSocket.Out<JsonNode>> userSocketsInRoom = rooms.get(roomId);

        if (userSocketsInRoom == null) {
            Logger.error("Not notifying rooms because rooms map does not contain room " + roomId);
            return;
        }

        final ObjectNode message = Json.newObject();
        message.put(EVENT_KEY, kind);
        message.put(MESSAGE_KEY, text);

        // If its not a talk or keepalive add the user to the message
        if (!Talk.TYPE.equals(kind) && userId != SocketKeepAlive.USER_ID) {
            User sender = findExistingEntityById(User.class, userId);
            message.set(USER_KEY, toJson(sender));
        }

        logV("About to notify room with: " + message);

        userSocketsInRoom.values().forEach(channel -> channel.write(message));

        Logger.debug("Notified users: " + userSocketsInRoom.keySet());
    }

// -- Messages

    public static class MessageListener extends JedisPubSub {

        @Override
        public void onMessage(String channel, String messageBody) {
            //Process messages from the pub/sub channel
            JsonNode parsedMessage = Json.parse(messageBody);

            Logger.debug("MessageListener.onMessage: " + parsedMessage);
            Object message;
            String messageType = parsedMessage.get("type").asText();

            switch (messageType) {
                case Talk.TYPE:
                    message = new Talk(
                            parsedMessage.get("roomId").asLong(),
                            parsedMessage.get("userId").asLong(),
                            parsedMessage.get("text").asText(),
                            parsedMessage.get("anon").asBoolean());
                    break;
                case RosterNotification.TYPE:
                    message = new RosterNotification(
                            parsedMessage.get("roomId").asLong(),
                            parsedMessage.get("userId").asLong(),
                            parsedMessage.get("direction").asText());
                    break;
                case Quit.TYPE:
                    message = new Quit(
                            parsedMessage.get("roomId").asLong(),
                            parsedMessage.get("userId").asLong());
                    break;
                case FavoriteNotification.TYPE:
                    message = new FavoriteNotification(
                            parsedMessage.get("userId").asLong(),
                            parsedMessage.get("messageId").asLong(),
                            parsedMessage.get("action").asText());
                    break;
                default:
                    throw new RuntimeException("Message type " + messageType + " is not supported");
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

    private static void logV(String msg) {
        if (VERBOSE) {
            Logger.debug(msg);
        }
    }

}