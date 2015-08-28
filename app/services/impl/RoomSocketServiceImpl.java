package services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.AbstractRoom;
import models.PublicRoom;
import models.User;
import play.db.jpa.JPA;
import play.libs.F;
import play.libs.Json;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import services.*;
import sockets.RoomSocket;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;
import static play.libs.Json.toJson;

/**
 * Created by kdoherty on 7/13/15.
 */
public class RoomSocketServiceImpl implements RoomSocketService {

    private final AbstractRoomService abstractRoomService;
    private final PublicRoomService publicRoomService;
    private final JedisService jedisService;
    private final AnonUserService anonUserService;
    private final UserService userService;

    @Inject
    public RoomSocketServiceImpl(AbstractRoomService abstractRoomService, PublicRoomService publicRoomService, UserService userService, AnonUserService anonUserService, JedisService jedisService) {
        this.abstractRoomService = abstractRoomService;
        this.publicRoomService = publicRoomService;
        this.userService = userService;
        this.anonUserService = anonUserService;
        this.jedisService = jedisService;
    }

    @Override
    public void join(long roomId, long userId, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception {
        String result = (String) Await.result(ask(RoomSocket.defaultRoom, new RoomSocket.Join(roomId, userId, out), 3000), Duration.create(3, SECONDS));

        if (RoomSocket.OK_JOIN_RESULT.equals(result)) {

            jedisService.useJedisResource(jedis -> {
                ObjectNode joinSuccessEvent = getSuccessfulJoinEvent(roomId, userId, jedis);
                out.write(joinSuccessEvent);
                in.onMessage(new SocketMessageReceiver(roomId, userId, jedis));
            });

            // When the socket is closed.
            in.onClose(() -> RoomSocket.defaultRoom.tell(new RoomSocket.Quit(roomId, userId), null));

        } else {
            ObjectNode error = getFailedJoinEvent(result);
            // Send the error to socket of the user who is trying to connect.
            out.write(error);
        }
    }

    private ObjectNode getSuccessfulJoinEvent(long roomId, long userId, Jedis jedis) {

        ObjectNode event = Json.newObject();
        event.put(RoomSocket.EVENT_KEY, "joinSuccess");

        JPA.withTransaction(() -> {
            // All room members not including the user themselves

            Optional<AbstractRoom> roomOptional = abstractRoomService.findById(roomId);
            AbstractRoom room = roomOptional.orElseThrow(RuntimeException::new);

            if (room instanceof PublicRoom) {
                List<User> roomMembers = RoomSocket.getUserIdsInRoomStream(roomId, jedis)
                        .filter(id -> id != KeepAliveService.ID && id != userId)
                        .map(userService::findById)
                        .map(Optional::get)
                        .collect(Collectors.<User>toList());

                ObjectNode message = Json.newObject();
                message.set("roomMembers", toJson(roomMembers));

                boolean isSubscribed = publicRoomService.isSubscribed((PublicRoom) room, userId);
                message.put("isSubscribed", isSubscribed);

                Optional<User> user = userService.findById(userId);
                message.set("anonUser", toJson(anonUserService.getOrCreateAnonUser(user.get(), (PublicRoom) room)));

                event.set(RoomSocket.MESSAGE_KEY, message);
            }

        });

        return event;
    }

    private ObjectNode getFailedJoinEvent(String result) {
        return Json.newObject()
                .put(RoomSocket.EVENT_KEY, "error")
                .put(RoomSocket.MESSAGE_KEY, result);
    }


    private class SocketMessageReceiver implements F.Callback<JsonNode> {

        private long roomId;
        private long userId;
        private Jedis jedis;

        private SocketMessageReceiver(long roomId, long userId, Jedis jedis) {
            this.roomId = roomId;
            this.userId = userId;
            this.jedis = jedis;
        }

        @Override
        public void invoke(JsonNode message) throws Throwable {
            final String event = message.get("event").asText();

            Object messageObject;
            switch (event) {
                case RoomSocket.Talk.TYPE:
                    messageObject = new RoomSocket.Talk(roomId, userId, message.get("message").asText(), message.get("isAnon").asBoolean(), message.get("uuid").asText());
                    break;
                case RoomSocket.FavoriteNotification.TYPE:
                    messageObject = new RoomSocket.FavoriteNotification(userId, Long.parseLong(message.get("messageId").asText()), message.get("action").asText());
                    break;
                default:
                    throw new RuntimeException("Event: " + event + " is not supported");
            }

            jedis.publish(RoomSocket.CHANNEL, Json.stringify(toJson(messageObject)));
        }
    }
}
