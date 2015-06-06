package models.sockets;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.plugin.RedisPlugin;
import models.entities.*;
import models.sockets.messages.*;
import org.apache.commons.lang3.tuple.Pair;
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
import utils.NotificationUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;
import static play.libs.Json.toJson;
import static utils.DbUtils.findExistingEntityById;

public class RoomSocket extends UntypedActor {

    private static final String CHANNEL = "messages";

    private static final String OK_JOIN_RESULT = "OK";

    private static final String EVENT_KEY = "event";
    private static final String MESSAGE_KEY = "message";
    private static final String USER_KEY = "user";

    static final ActorRef defaultRoom = Akka.system().actorOf(Props.create(RoomSocket.class));

    // Key is roomId, value is the users connected to that room
    static final Map<Long, Map<Long, WebSocket.Out<JsonNode>>> rooms = new HashMap<>();

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

        if (OK_JOIN_RESULT.equals(result)) {

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
                    event.put(EVENT_KEY, "joinSuccess");

                    ObjectNode message = Json.newObject();
                    message.put("roomMembers", toJson(roomMembers));

                    AbstractRoom room = findExistingEntityById(AbstractRoom.class, roomId);
                    if (room instanceof PublicRoom) {
                        boolean isSubscribed = ((PublicRoom) room).isSubscribed(userId);
                        message.put("isSubscribed", isSubscribed);
                    }

                    event.put(MESSAGE_KEY, message);

                    out.write(event);
                });
            } finally {
                play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
            }

            // For each event received on the socket,
            in.onMessage(message -> {

                final String event = message.get("event").asText();

                Object messageObject;
                switch (event) {
                    case Talk.TYPE:
                        Logger.debug("onMessage message " + message);
                        if (message.has("isAnon") && message.get("isAnon").asBoolean()) {
                            messageObject = new Talk(roomId, userId, message.get("message").asText(), message.get("isAnon").asBoolean());
                        } else {
                            messageObject = new Talk(roomId, userId, message.get("message").asText());
                        }
                        Logger.debug("onMessage messageObject" + messageObject);
                        break;
                    case FavoriteNotification.TYPE:
                        messageObject = new FavoriteNotification(userId, Long.parseLong(message.get("messageId").asText()), message.get("action").asText());
                        break;
                    default:
                        throw new RuntimeException("Event: " + event + " is not supported");
                }

                Jedis jedis = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();
                try {
                    jedis.publish(RoomSocket.CHANNEL, Json.stringify(toJson(messageObject)));
                } finally {
                    play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
                }
            });

            // When the socket is closed.
            in.onClose(() -> defaultRoom.tell(new Quit(roomId, userId), null));
        } else {
            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put(EVENT_KEY, "error");
            error.put(MESSAGE_KEY, result);

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
                receiveJoin(j, (Join) message);
            } else if (message instanceof Quit) {
                receiveQuit(j, (Quit) message);
            } else if (message instanceof RosterNotification) {
                receiveRosterNotification((RosterNotification) message);
            } else if (message instanceof Talk) {
                receiveTalk(j, (Talk) message);
            } else if (message instanceof FavoriteNotification) {
                receiveFavoriteNotification(j, (FavoriteNotification) message);
            } else {
                unhandled(message);
            }
        } catch (Throwable throwable) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            Logger.error("Problem receiving message: " + sw.toString());
        } finally {
            play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
        }
    }

    private void receiveFavoriteNotification(Jedis j, FavoriteNotification favoriteNotification) throws Throwable {
        Logger.debug("ReceiveFavoriteNotification: " + favoriteNotification);

        JPA.withTransaction(() -> {
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
                long roomId = message.room.roomId;
                notifyRoom(roomId, favoriteNotification.getAction().getType(), userId, String.valueOf(messageId));

                Set<Long> userIdsInRoom = j.smembers(String.valueOf(roomId))
                        .stream()
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());

                if (!userIdsInRoom.contains(message.senderId)) {
                    NotificationUtils.sendMessageFavorited(user, message);
                }
            } else {
                ObjectNode error = Json.newObject();
                error.put(EVENT_KEY, "error");
                error.put(MESSAGE_KEY, "Problem " + favoriteNotification.getAction() + "ing a favorite");

                notifyUser(message.room.roomId, userId, error);
            }
        });
    }


    private void receiveTalk(Jedis j, Talk talk) throws Throwable {
        Logger.debug("receiveTalk: " + talk);

        long roomId = talk.getRoomId();
        long userId = talk.getUserId();
        String messageText = talk.getText();

        if (userId == SocketKeepAlive.USER_ID) {
            notifyRoom(roomId, Talk.TYPE, userId, messageText);
            return;
        }

        Pair<Message, User> pair = storeMessage(talk);
        Message message = pair.getLeft();
        User messageSender = pair.getRight();

        ObjectNode senderJson = Json.newObject();
        senderJson.put("id", message.senderId);
        senderJson.put("name", message.senderName);
        senderJson.put("fbId", message.senderFbId);

        ObjectNode messageJson = Json.newObject();
        messageJson.put("isAnon", message.isAnon);
        messageJson.put("message", message.message);
        messageJson.put("sender", senderJson);

        notifyRoom(roomId, Talk.TYPE, userId, Json.stringify(toJson(message)));
        notifyRoomSubscribers(message.room, messageSender, message, j);
    }

    private void notifyUser(long roomId, long userId, JsonNode message) {
        Map<Long, WebSocket.Out<JsonNode>> room = rooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("Can't notify user because room " + roomId + " does not exist in rooms");
        }

        WebSocket.Out<JsonNode> nodeOut = room.get(userId);

        if (nodeOut == null) {
            throw new RuntimeException("Can't notify user " + userId + " because they are not in room " + roomId);
        }

        nodeOut.write(message);
    }

    private Pair<Message, User> storeMessage(Talk talk) throws Throwable {
        final long senderId = talk.getUserId();
        final long roomId = talk.getRoomId();
        final boolean isAnon = talk.isAnon();

        return JPA.withTransaction(() -> {
            User sender = DbUtils.findExistingEntityById(User.class, senderId);
            String senderName = sender.name;
            String facebookId = sender.facebookId;
            long userId = senderId;

            if (isAnon) {
                UserAlias userAlias = UserAlias.getOrCreateAlias(senderId, roomId);
                senderName = userAlias.alias;
                facebookId = null;
                userId = userAlias.userAliasId;
            }

            Message message = new Message(roomId, userId, senderName, facebookId, talk.getText(), isAnon);
            message.addToRoom();
            return Pair.of(message, sender);
        });
    }

    private void notifyRoomSubscribers(AbstractRoom room, User messageSender, Message message, Jedis j) throws Throwable {

        if (room instanceof PublicRoom && !message.isAnon) {
            PublicRoom publicRoom = (PublicRoom) room;

            if (publicRoom.hasSubscribers()) {
                Set<Long> userIdsInRoom = j.smembers(String.valueOf(publicRoom.roomId))
                        .stream()
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());

                NotificationUtils.messageSubscribers(publicRoom, messageSender, message.message, userIdsInRoom);
            }
        } else if (room instanceof PrivateRoom) {
            PrivateRoom privateRoom = (PrivateRoom) room;
            long recipientId = privateRoom.sender.userId == message.senderId ?
                    privateRoom.receiver.userId : privateRoom.sender.userId;

            Set<String> roomMembers = j.smembers(String.valueOf(room.roomId));
            if (!roomMembers.contains(String.valueOf(recipientId))) {
                NotificationUtils.messageUser(privateRoom, messageSender, recipientId, message.message);
            }
        }
    }

    private void receiveJoin(Jedis j, Join join) {
        Logger.debug("receiveJoin: " + join);
        long roomId = join.getRoomId();
        long userId = join.getUserId();
        if (j.sismember(String.valueOf(roomId), String.valueOf(userId))) {
            getSender().tell("This userId is already used", getSelf());
            Logger.error("User " + userId + " is trying to join room: " + roomId + " but the userId is already in use");
        } else {
            if (!rooms.containsKey(roomId)) {
                // Creating a new room
                Logger.debug("Adding new room " + roomId + " and adding a keep alive");

                rooms.put(roomId, new HashMap<>());

                addKeepAlive(roomId);
            }
            //Add the member to this node and the global roster
            rooms.get(roomId).put(userId, join.getChannel());
            j.sadd(String.valueOf(roomId), String.valueOf(userId));

            //Publish the join notification to all nodes
            RosterNotification rosterNotify = new RosterNotification(roomId, userId, Join.TYPE);
            j.publish(RoomSocket.CHANNEL, Json.stringify(toJson(rosterNotify)));

            getSender().tell(OK_JOIN_RESULT, getSelf());
        }
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

        j.srem(String.valueOf(roomId), String.valueOf(userId));

        Set<String> roomMembers = j.smembers(String.valueOf(roomId));

        Logger.debug("After quit room members are: " + roomMembers);

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
        } else {
            //Publish the quit notification to all nodes
            RosterNotification rosterNotify = new RosterNotification(roomId, userId, Quit.TYPE);
            j.publish(RoomSocket.CHANNEL, Json.stringify(toJson(rosterNotify)));
        }
    }

    private void receiveRosterNotification(RosterNotification rosterNotification) throws Throwable {
        Logger.debug("receiveRosterNotification: " + rosterNotification);
        if (Join.TYPE.equals(rosterNotification.getDirection())) {
            notifyRoom(rosterNotification.getRoomId(), Join.TYPE, rosterNotification.getUserId(), "has entered the room");
        } else if (Quit.TYPE.equals(rosterNotification.getDirection())) {
            notifyRoom(rosterNotification.getRoomId(), Quit.TYPE, rosterNotification.getUserId(), "has left the room");
        }
    }

    // Send a Json event to all members connected to this node
    public void notifyRoom(long roomId, String kind, long userId, String text) throws Throwable {
        Logger.debug("NotifyAll called with roomId: " + roomId + " and message: " + text);

        if (!rooms.containsKey(roomId)) {
            Logger.error("Not notifying rooms because rooms map does not contain room " + roomId);
            return;
        }

        final ObjectNode message = Json.newObject();
        message.put(EVENT_KEY, kind);
        message.put(MESSAGE_KEY, text);

        // If its not a talk or keepalive add the user to the message
        if (!Talk.TYPE.equals(kind) && userId != SocketKeepAlive.USER_ID) {
            User sender = JPA.withTransaction(() -> findExistingEntityById(User.class, userId));
            message.put(USER_KEY, toJson(sender));
        }

        rooms.get(roomId).values().stream().forEach(channel -> channel.write(message));
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

}