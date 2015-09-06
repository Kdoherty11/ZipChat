package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import models.*;
import play.Logger;
import play.api.Play;
import play.db.jpa.JPA;
import play.libs.Json;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import scala.concurrent.duration.Duration;
import services.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static play.libs.Json.toJson;

public class RoomActor extends UntypedActor {

    public static final String CHANNEL = "messages";

    public static final String OK_JOIN_RESULT = "OK";

    public static final String EVENT_KEY = "event";
    public static final String MESSAGE_KEY = "message";
    public static final String USER_KEY = "user";

    public static final ActorRef defaultRoom = Play.current().actorSystem().actorOf(
            Props.create(RoomActor.class,
                    () -> Play.current().injector().instanceOf(RoomActor.class)));

    // Key is roomId, value is the users connected to that room
    private static final Map<Long, Map<Long, WebSocket.Out<JsonNode>>> rooms = new ConcurrentHashMap<>();

    private static final boolean VERBOSE = false;

    static {
        //subscribe to the message channel
        ActorSystem actorSystem = Play.current().actorSystem();
        actorSystem.scheduler().scheduleOnce(
                Duration.create(10, TimeUnit.MILLISECONDS),
                () -> {
                    JedisService service = Play.current().injector().instanceOf(JedisService.class);
                    service.useJedisResource(jedis -> jedis.subscribe(new MessageListener(), CHANNEL));
                },
                actorSystem.dispatcher()
        );
    }

    private final AbstractRoomService abstractRoomService;

    private final AnonUserService anonUserService;

    private final MessageService messageService;

    private final JedisService jedisService;

    private final UserService userService;

    private final KeepAliveService keepAliveService;

    @Inject
    public RoomActor(final AbstractRoomService abstractRoomService, final UserService userService,
                     final AnonUserService anonUserService, final MessageService messageService,
                     final JedisService jedisService, final KeepAliveService keepAliveService) {
        this.abstractRoomService = abstractRoomService;
        this.userService = userService;
        this.anonUserService = anonUserService;
        this.messageService = messageService;
        this.jedisService = jedisService;
        this.keepAliveService = keepAliveService;
    }

    public static Stream<Long> getUserIdsInRoomStream(long roomId, Jedis j) {
        return j.smembers(Long.toString(roomId))
                .stream()
                .map(Long::parseLong);
    }

    private Set<Long> getUserIdsInRoom(long roomId, Jedis j) {
        return getUserIdsInRoomStream(roomId, j).collect(Collectors.toSet());
    }

    public static void remoteMessage(Object message) {
        defaultRoom.tell(message, null);
    }

    @Override
    public void onReceive(Object message) {
        logV("onReceive: " + message);
        if (message instanceof Join) {
            jedisService.useJedisResource(jedis -> receiveJoin((Join) message, jedis));
        } else if (message instanceof Quit) {
            jedisService.useJedisResource(jedis -> receiveQuit((Quit) message, jedis));
        } else if (message instanceof RosterNotification) {
            JPA.withTransaction(() -> {
                receiveRosterNotification((RosterNotification) message);
            });
        } else if (message instanceof Talk) {
            JPA.withTransaction(() -> {
                jedisService.useJedisResource(jedis -> receiveTalk((Talk) message, jedis));
            });
        } else if (message instanceof FavoriteNotification) {
            JPA.withTransaction(() -> {
                receiveFavoriteNotification((FavoriteNotification) message);
            });
        } else {
            Logger.error("Unhandled message received: " + message);
            unhandled(message);
        }
    }

    private void receiveTalk(Talk talk, Jedis jedis) {
        //Logger.debug("receiveTalk: " + talk);

        long roomId = talk.getRoomId();
        long userId = talk.getUserId();
        String messageText = talk.getText();

        if (userId == KeepAliveService.ID) {
            notifyRoom(roomId, Talk.TYPE, userId, messageText);
            return;
        }

        try {
            Message message = JPA.withTransaction(() -> storeMessage(talk, jedis));
            String messageJsonStr = Json.stringify(toJson(message));
            notifyRoom(roomId, Talk.TYPE, userId, messageJsonStr);

            JsonNode talkConfirmation = Json.newObject()
                    .put(EVENT_KEY, "talk-confirmation")
                    .put(MESSAGE_KEY, messageJsonStr)
                    .put("uuid", talk.getUuid());

            notifyUser(roomId, userId, talkConfirmation);
        } catch (Throwable throwable) {
            Logger.error("Problem storing the message", throwable);
            throw new RuntimeException("Problem storing the message", throwable);
        }
    }

    private void receiveFavoriteNotification(FavoriteNotification favoriteNotification) {
        //Logger.debug("ReceiveFavoriteNotification: " + favoriteNotification);

        long messageId = favoriteNotification.getMessageId();
        Message message = messageService.findById(messageId).get();

        long userId = favoriteNotification.getUserId();
        final User user = userService.findById(userId).get();

        boolean success;
        if (favoriteNotification.getAction() == FavoriteNotification.Action.ADD) {
            success = messageService.favorite(message, user);
        } else {
            success = messageService.removeFavorite(message, user);
        }

        if (success) {
            notifyRoom(message.room.roomId, favoriteNotification.getAction().getType(), userId, Long.toString(messageId));
        } else {
            ObjectNode error = Json.newObject();
            error.put(EVENT_KEY, "error");
            error.put(MESSAGE_KEY, "Problem " + favoriteNotification.getAction() + "ing a favorite");

            notifyUser(message.room.roomId, userId, error);
        }
    }

    private void notifyUser(long roomId, long userId, JsonNode message) {
        //Logger.debug("sending " + message + " to user " + userId);
        Optional<WebSocket.Out<JsonNode>> outOptional = getWebSocket(roomId, userId);

        // Won't always be present if user is on different dyno
        outOptional.ifPresent(outSocket -> outSocket.write(message));
    }

    private Optional<WebSocket.Out<JsonNode>> getWebSocket(long roomId, long userId) {
        Map<Long, WebSocket.Out<JsonNode>> room = rooms.get(roomId);
        if (room == null) {
            Logger.error("Could not find room " + roomId + " in room map");
            return Optional.empty();
        }
        return Optional.ofNullable(room.get(userId));
    }

    private Message storeMessage(Talk talk, Jedis jedis) {
        final long senderId = talk.getUserId();
        final long roomId = talk.getRoomId();
        final boolean isAnon = talk.isAnon();
        User sender = userService.findById(senderId).get();
        AbstractRoom room = abstractRoomService.findById(roomId).get();

        AbstractUser messageSender;
        if (isAnon) {
            if (room instanceof PublicRoom) {
                messageSender = anonUserService.getOrCreateAnonUser(sender, (PublicRoom) room);
            } else {
                throw new RuntimeException("Trying to store an anon message in a private room");
            }
        } else {
            messageSender = sender;
        }

        Message message = new Message(room, messageSender, talk.getText());
        abstractRoomService.addMessage(room, message, getUserIdsInRoom(roomId, jedis));

        return message;
    }

    private void receiveJoin(Join join, Jedis jedis) {
        Logger.debug("receiveJoin: " + join);
        long roomId = join.getRoomId();
        long userId = join.getUserId();

        if (!rooms.containsKey(roomId)) {
            // Creating a new room
            //Logger.debug("Adding new room " + roomId + " and adding a keep alive");
            rooms.put(roomId, new HashMap<>());
            keepAliveService.start(roomId);
        }

        if (jedis.sismember(Long.toString(roomId), Long.toString(userId))) {
            Logger.error("User " + userId + " is trying to join room: " + roomId + " but the userId is already in use");
        } else {
            jedis.sadd(Long.toString(roomId), Long.toString(userId));
        }

        rooms.get(roomId).put(userId, join.getChannel());

        //Publish the join notification to all nodes
        RosterNotification rosterNotify = new RosterNotification(roomId, userId, RosterNotification.Direction.JOIN);
        jedis.publish(RoomActor.CHANNEL, Json.stringify(toJson(rosterNotify)));

        getSender().tell(OK_JOIN_RESULT, getSelf());

        Logger.debug("Join success");
    }

    private void receiveQuit(Quit quit, Jedis jedis) {
        Logger.debug("receiveQuit: " + quit);
        long roomId = quit.getRoomId();
        long userId = quit.getUserId();

        Map<Long, WebSocket.Out<JsonNode>> members = rooms.get(roomId);

        if (members != null) {
            members.remove(userId);
        }

        if (members == null || members.isEmpty()) {
            keepAliveService.stop(roomId);
            rooms.remove(roomId);
        }

        jedis.srem(Long.toString(roomId), Long.toString(userId));

        // Still need to publish to jedis even if there are no more users connected to this dyno
        RosterNotification rosterNotify = new RosterNotification(roomId, userId, RosterNotification.Direction.QUIT);
        jedis.publish(RoomActor.CHANNEL, Json.stringify(toJson(rosterNotify)));

        Logger.debug("Quit success");
    }

    private void receiveRosterNotification(RosterNotification rosterNotification) {
        //Logger.debug("receiveRosterNotification: " + rosterNotification);

        RosterNotification.Direction direction = rosterNotification.getDirection();
        notifyRoom(rosterNotification.getRoomId(), direction.getType(),
                rosterNotification.getUserId(), direction.getUpdateMessage());
    }

    // Send a Json event to all members connected to this node
    public void notifyRoom(long roomId, String kind, long userId, String text) {
        //Logger.debug("NotifyAll called with kind: " + kind + ", roomId: " + roomId + " and message: " + text);

        Map<Long, WebSocket.Out<JsonNode>> userSocketsInRoom = rooms.get(roomId);

        if (userSocketsInRoom == null) {
            // There are no users connected to this dyno in this room
            return;
        }

        final ObjectNode message = Json.newObject();
        message.put(EVENT_KEY, kind);
        message.put(MESSAGE_KEY, text);

        boolean isTalk = Talk.TYPE.equals(kind);


        // If its not a talk add the user to the message
        if (!isTalk) {
            User sender = userService.findById(userId).get();
            message.set(USER_KEY, toJson(sender));
        }

        logV("About to notify room with: " + message);

        userSocketsInRoom.entrySet().forEach(entry -> {
            // Not sending talks to the sending user...
            // They should get talk-confirmations instead
            if (!isTalk || userId != entry.getKey()) {
                entry.getValue().write(message);
            }
        });

        //Logger.debug("Notified users: " + userSocketsInRoom.keySet());
    }

// -- Messages

    public static class MessageListener extends JedisPubSub {

        @Override
        public void onMessage(String channel, String messageBody) {
            //Process messages from the pub/sub channel
            JsonNode parsedMessage = Json.parse(messageBody);

            //Logger.debug("MessageListener.onMessage: " + parsedMessage);
            Object message;
            String messageType = parsedMessage.get("type").asText();

            switch (messageType) {
                case Talk.TYPE:
                    message = new Talk(
                            parsedMessage.get("roomId").asLong(),
                            parsedMessage.get("userId").asLong(),
                            parsedMessage.get("text").asText(),
                            parsedMessage.get("anon").asBoolean(),
                            parsedMessage.get("uuid").asText());
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
            remoteMessage(message);
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

    public static class Join {

        public static final String TYPE = "join";

        private long roomId;
        private long userId;
        private WebSocket.Out<JsonNode> channel;

        // For JSON serialization
        final String type = TYPE;

        public Join() {
            // required default constructor
        }

        public Join(long roomId, long userId, WebSocket.Out<JsonNode> channel) {
            this.roomId = roomId;
            this.userId = userId;
            this.channel = channel;
        }

        public long getRoomId() {
            return roomId;
        }

        public long getUserId() {
            return userId;
        }

        public String getType() {
            return type;
        }

        public WebSocket.Out<JsonNode> getChannel() {
            return channel;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("roomId", roomId)
                    .add("userId", userId)
                    .toString();
        }
    }

    public static class RosterNotification {

        public static final String TYPE = "rosterNotify";

        enum Direction {
            JOIN(Join.TYPE, "has entered the room"),
            QUIT(Quit.TYPE, "has left the room");

            private String type;
            private String updateMessage;

            Direction(String type, String updateMessage) {
                this.updateMessage = updateMessage;
                this.type = type;
            }

            public String getUpdateMessage() {
                return updateMessage;
            }

            public String getType() {
                return type;
            }
        }

        long roomId;
        long userId;
        Direction direction;

        // For JSON serialization
        final String type = TYPE;

        public RosterNotification() {

        }

        public RosterNotification(long roomId, long userId, Direction direction) {
            this.roomId = roomId;
            this.userId = userId;
            this.direction = direction;
        }

        public RosterNotification(long roomId, long userId, String direction) {
            this(roomId, userId, Direction.valueOf(direction.toUpperCase()));
        }

        public long getUserId() {
            return userId;
        }

        public Direction getDirection() {
            return direction;
        }

        public long getRoomId() {
            return roomId;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("roomId", roomId)
                    .add("userId", userId)
                    .add("direction", direction)
                    .toString();
        }
    }

    public static class Talk {

        public static final String TYPE = "talk";

        long roomId;
        long userId;
        String text;
        boolean isAnon;
        private String uuid;

        // For JSON serialization
        final String type = TYPE;

        public Talk() {
            // required default constructor
        }

        public Talk(long roomId, long userId, String text, boolean isAnon, String uuid) {
            this.roomId = roomId;
            this.userId = userId;
            this.text = text;
            this.isAnon = isAnon;
            this.uuid = uuid;
        }

        public Talk(long roomId, long userId, String text, String uuid) {
            this(roomId, userId, text, false, uuid);
        }

        public long getUserId() {
            return userId;
        }

        public String getText() {
            return text;
        }

        public long getRoomId() {
            return roomId;
        }

        public String getType() {
            return type;
        }

        public String getUuid() {
            return uuid;
        }

        public boolean isAnon() {
            return isAnon;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("roomId", roomId)
                    .add("userId", userId)
                    .add("text", text)
                    .add("isAnon", isAnon)
                    .add("uuid", uuid)
                    .toString();
        }
    }

    public static class FavoriteNotification {

        public static final String TYPE = "FavoriteNotification";

        public enum Action {

            ADD("favorite"),
            REMOVE("removeFavorite");

            private String type;

            Action(String type) {
                this.type = type;
            }

            public String getType() {
                return type;
            }

            @Override
            public String toString() {
                return name().toLowerCase();
            }
        }

        private long messageId;
        private long userId;
        private Action action;

        // For JSON serialization
        final String type = TYPE;

        public FavoriteNotification() {
            // required default constructor
        }

        public FavoriteNotification(long userId, long messageId, Action action) {
            this.userId = userId;
            this.messageId = messageId;
            this.action = action;
        }

        public FavoriteNotification(long userId, long messageId, String actionString) {
            this(userId, messageId, Action.valueOf(actionString.toUpperCase()));
        }

        public long getMessageId() {
            return messageId;
        }

        public long getUserId() {
            return userId;
        }

        public Action getAction() {
            return action;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("messageId", messageId)
                    .add("userId", userId)
                    .add("action", action)
                    .add("type", type)
                    .toString();
        }
    }

    public static class Quit {

        public static final String TYPE = "quit";

        private long roomId;
        private long userId;

        // For JSON serialization
        final String type = TYPE;

        public Quit() {
            // required default constructor
        }

        public Quit(long roomId, long userId) {
            this.roomId = roomId;
            this.userId = userId;
        }

        public long getUserId() {
            return userId;
        }

        public long getRoomId() {
            return roomId;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("roomId", roomId)
                    .add("userId", userId)
                    .toString();
        }

    }


}