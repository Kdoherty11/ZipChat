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
import services.AbstractRoomService;
import services.JedisService;
import services.PublicRoomService;
import services.RoomSocketService;
import sockets.RoomSocket;

import java.util.List;
import java.util.Optional;

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

    @Inject
    public RoomSocketServiceImpl(AbstractRoomService abstractRoomService, PublicRoomService publicRoomService, JedisService jedisService) {
        this.abstractRoomService = abstractRoomService;
        this.publicRoomService = publicRoomService;
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

        List<User> roomMembers = RoomSocket.getRoomMembers(roomId, jedis);

        ObjectNode event = Json.newObject();
        event.put(RoomSocket.EVENT_KEY, "joinSuccess");

        ObjectNode message = Json.newObject();
        message.set("roomMembers", toJson(roomMembers));

        JPA.withTransaction(() -> {
            Optional<AbstractRoom> roomOptional = abstractRoomService.findById(roomId);
            AbstractRoom room = roomOptional.orElseThrow(RuntimeException::new);
            if (room instanceof PublicRoom) {
                boolean isSubscribed = publicRoomService.isSubscribed((PublicRoom) room, userId);
                message.put("isSubscribed", isSubscribed);
            }
        });

        event.set(RoomSocket.MESSAGE_KEY, message);

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
                    if (message.has("isAnon") && message.get("isAnon").asBoolean()) {
                        messageObject = new RoomSocket.Talk(roomId, userId, message.get("message").asText(), true);
                    } else {
                        messageObject = new RoomSocket.Talk(roomId, userId, message.get("message").asText());
                    }
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
