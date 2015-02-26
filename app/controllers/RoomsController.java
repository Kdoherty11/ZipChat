package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.RoomSocket;
import models.entities.Message;
import models.entities.Room;
import models.entities.User;
import play.Logger;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.DbUtils;

import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;


public class RoomsController extends BaseController {

    /**
     * Handle the chat websocket.
     */
    public static WebSocket<JsonNode> joinRoom(final String roomId, final String username) {

        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                Logger.debug("join" + roomId + " " + username);
                try {
                    RoomSocket.join(roomId, username, in, out);
                } catch (Exception ex) {
                    Logger.error("Problem joining the RoomSocket: " + ex.getMessage());
                }
            }
        };
    }

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
        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);

        if (roomOptional.isPresent()) {

            Map<String, String> formData = form().bindFromRequest().data();

            if (!formData.containsKey("userId")) {
                return badRequestJson("Field userId is required");
            }

            String userId = formData.get("userId");

            Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);

            if (userOptional.isPresent()) {
                roomOptional.get().addSubscription(userOptional.get());

                return okJson("OK");
            } else {
                return badRequestJson(DbUtils.buildEntityNotFoundError("User", userId));
            }
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError("Room", roomId));
        }
    }

    @Transactional
    public static Result getSubscriptions(String roomId) {
        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);

        if (roomOptional.isPresent()) {
            return okJson(roomOptional.get().subscribers);
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError("Room", roomId));
        }
    }

    @Transactional
    public static Result createMessage(String roomId) {
        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);

        if (roomOptional.isPresent()) {

            Form<Message> form = form(Message.class).bindFromRequest();

            if (form.hasErrors()) {
                return badRequest(form.errorsAsJson());
            }

            Message message = form.get();

            JPA.em().persist(message);

            roomOptional.get().addMessage(message);

            return okJson(message);
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError("Room", roomId));
        }
    }

    @Transactional
    public static Result getMessages(String roomId) {
        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);

        if (roomOptional.isPresent()) {
            return okJson(roomOptional.get().messages);
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError("Room", roomId));
        }
    }

}
