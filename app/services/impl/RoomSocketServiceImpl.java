package services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import models.entities.AbstractRoom;
import models.entities.PublicRoom;
import models.entities.User;
import models.sockets.RoomSocket;
import models.sockets.events.FavoriteNotification;
import models.sockets.events.Join;
import models.sockets.events.Quit;
import models.sockets.events.Talk;
import play.db.jpa.JPA;
import play.libs.Json;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import services.AbstractRoomService;
import services.PublicRoomService;
import services.RoomSocketService;

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
    private final JedisPool jedisPool;

    @Inject
    public RoomSocketServiceImpl(AbstractRoomService abstractRoomService, PublicRoomService publicRoomService, JedisPool jedisPool) {
        this.abstractRoomService = abstractRoomService;
        this.publicRoomService = publicRoomService;
        this.jedisPool = jedisPool;
    }

    @Override
    public void join(long roomId, long userId, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception {
        String result = (String) Await.result(ask(RoomSocket.defaultRoom, new Join(roomId, userId, out), 3000), Duration.create(3, SECONDS));

        if (RoomSocket.OK_JOIN_RESULT.equals(result)) {
            final Jedis jedis = jedisPool.getResource();
            boolean returnedRedisResource = false;
            try {
                JPA.withTransaction(() -> {

                    List<User> roomMembers = RoomSocket.getRoomMembers(roomId, jedis);

                    ObjectNode event = Json.newObject();
                    event.put(RoomSocket.EVENT_KEY, "joinSuccess");

                    ObjectNode message = Json.newObject();
                    message.set("roomMembers", toJson(roomMembers));

                    Optional<AbstractRoom> roomOptional = abstractRoomService.findById(roomId);
                    AbstractRoom room = roomOptional.orElseThrow(RuntimeException::new);
                    if (room instanceof PublicRoom) {
                        boolean isSubscribed = publicRoomService.isSubscribed((PublicRoom) room, userId);
                        message.put("isSubscribed", isSubscribed);
                    }

                    event.set(RoomSocket.MESSAGE_KEY, message);

                    out.write(event);
                });

                // For each event received on the socket,
                in.onMessage(message -> {

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

                    jedis.publish(RoomSocket.CHANNEL, Json.stringify(toJson(messageObject)));
                });

                // When the socket is closed.
                in.onClose(() -> RoomSocket.defaultRoom.tell(new Quit(roomId, userId), null));

            } catch (JedisConnectionException e) {
                if (jedis != null) {
                    jedisPool.returnBrokenResource(jedis);
                    returnedRedisResource = true;
                }
            } finally {
                if (!returnedRedisResource) {
                    jedisPool.returnResource(jedis);
                }
            }
        } else {
            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put(RoomSocket.EVENT_KEY, "error");
            error.put(RoomSocket.MESSAGE_KEY, result);

            // Send the error to the socket.
            out.write(error);
        }
    }
}
