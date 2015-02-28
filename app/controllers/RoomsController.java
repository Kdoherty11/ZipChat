package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.RoomSocket;
import models.entities.Message;
import models.entities.Room;
import models.entities.User;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.DbUtils;

import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;


public class RoomsController extends BaseController {

    @Transactional
    public static Result createRoom() {
        return create(Room.class);
    }

    @Transactional
    public static Result getRooms() {
        return read(Room.class);
    }

    @Transactional
    public static Result updateRoom(String id) {
        return update(Room.class, id);
    }

    @Transactional
    public static Result showRoom(String id) {
        return show(Room.class, id);
    }

    @Transactional
    public static Result deleteRoom(String id) {
        return delete(Room.class, id);
    }

    @Transactional
    public static Result getGeoRooms(double lat, double lon) {
        return okJson(Room.allInGeoRange(lat, lon));
    }

    @Transactional
    public static Result createSubscription(String roomId) {
        Map<String, String> data = form().bindFromRequest().data();

        String userIdKey = "userId";
        if (!data.containsKey(userIdKey)) {
            return badRequestJson(userIdKey + " is required");
        }

        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);
        if (roomOptional.isPresent()) {
            String userId = data.get(userIdKey);

            Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);
            if (userOptional.isPresent()) {
                roomOptional.get().addSubscription(userOptional.get());
                return okJson("OK");
            } else {
                return badRequestJson(DbUtils.buildEntityNotFoundError(User.ENTITY_NAME, userId));
            }
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError(Room.ENTITY_NAME, roomId));
        }
    }

    @Transactional
    public static Result getSubscriptions(String roomId) {
        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);

        if (roomOptional.isPresent()) {
            return okJson(roomOptional.get().subscribers);
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError(Room.ENTITY_NAME, roomId));
        }
    }

    @Transactional
    public static Result notifySubscribers(String roomId) {
        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);
        if (roomOptional.isPresent()) {
            roomOptional.get().notifySubscribers(form().bindFromRequest().data());
            return okJson("OK");
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError(Room.ENTITY_NAME, roomId));
        }
    }

    @Transactional
    public static Result createMessage(String roomId) {
        Map<String, String> data = form().bindFromRequest().data();

        String userIdKey = "userId";
        String messageKey = "message";

        if (!data.containsKey(userIdKey)) {
            return badRequestJson(userIdKey + " is required");
        }

        if (!data.containsKey(messageKey)) {
            return badRequestJson(messageKey + " is required");
        }

        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);

        if (roomOptional.isPresent()) {
            Message message = new Message(roomId, data.get(userIdKey), data.get(messageKey));
            JPA.em().persist(message);

            roomOptional.get().addMessage(message);

            return okJson(message);
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError(Room.ENTITY_NAME, roomId));
        }
    }

    @Transactional
    public static Result getMessages(String roomId) {
        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);

        if (roomOptional.isPresent()) {
            return okJson(roomOptional.get().messages);
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError(Room.ENTITY_NAME, roomId));
        }
    }

    @Transactional
    public static WebSocket<JsonNode> joinRoom(final String roomId, final String userId) {

        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                Logger.debug("joining " + roomId + " " + userId);
                try {
                    RoomSocket.join(roomId, userId, in, out);
                } catch (Exception ex) {
                    Logger.error("Problem joining the RoomSocket: " + ex.getMessage());
                }
            }
        };
    }
}