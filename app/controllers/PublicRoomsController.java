package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.Longs;
import models.entities.PublicRoom;
import models.entities.User;
import models.sockets.RoomSocket;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import security.Secured;
import security.SecurityHelper;
import utils.DbUtils;
import validation.DataValidator;
import validation.FieldValidator;
import validation.validators.Validators;

import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;

@Security.Authenticated(Secured.class)
public class PublicRoomsController extends BaseController {

    @Transactional
    public static Result createRoom() {
        return create(PublicRoom.class);
    }

    @Transactional
    public static Result getRooms() {
        return read(PublicRoom.class);
    }

    @Transactional
    public static Result getGeoRooms(double lat, double lon) {
        return okJson(PublicRoom.allInGeoRange(lat, lon));
    }

    @Transactional
    public static Result createSubscription(long roomId) {
        Map<String, String> data = form().bindFromRequest().data();

        String userIdKey = "userId";

        Long userId = Longs.tryParse(data.get(userIdKey));

        if (userId == null) {
            return FieldValidator.typeError(userIdKey, Long.class);
        }

        if (isUnauthorized(userId)) {
            return forbidden();
        }

        DataValidator validator = new DataValidator(
                new FieldValidator<>(userIdKey, userId, Validators.positive()));

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        Optional<PublicRoom> roomOptional = DbUtils.findEntityById(PublicRoom.class, roomId);
        if (roomOptional.isPresent()) {

            Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);
            if (userOptional.isPresent()) {
                roomOptional.get().addSubscription(userOptional.get());
                return OK_RESULT;
            } else {
                return DbUtils.getNotFoundResult(User.ENTITY_NAME, userId);
            }
        } else {
            return DbUtils.getNotFoundResult(PublicRoom.ENTITY_NAME, roomId);
        }
    }

    @Transactional
    public static Result removeSubscription(long roomId, long userId) {
        if (isUnauthorized(userId)) {
            return forbidden();
        }

        Optional<PublicRoom> roomOptional = DbUtils.findEntityById(PublicRoom.class, roomId);
        if (roomOptional.isPresent()) {
            roomOptional.get().removeSubscription(userId);
            return OK_RESULT;
        } else {
            return DbUtils.getNotFoundResult(PublicRoom.ENTITY_NAME, roomId);
        }
    }

    @Transactional
    public static Result isSubscribed(long roomId, long userId) {
        Optional<PublicRoom> roomOptional = DbUtils.findEntityById(PublicRoom.class, roomId);
        if (roomOptional.isPresent()) {
            return okJson(roomOptional.get().isSubscribed(userId));
        } else {
            return DbUtils.getNotFoundResult(PublicRoom.ENTITY_NAME, roomId);
        }
    }

    @Transactional
    public static WebSocket<JsonNode> joinRoom(final long roomId, final long userId, String authToken) {
        Optional<Long> userIdOptional = SecurityHelper.getUserId(authToken);
        if (!userIdOptional.isPresent() || userIdOptional.get() != userId) {
            return WebSocket.reject(forbidden());
        }

        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                try {
                    RoomSocket.join(roomId, userId, in, out);
                } catch (Exception ex) {
                    Logger.error("Problem joining the RoomSocket: " + ex.getMessage());
                }
            }
        };
    }

    @Transactional
    public static Result getMessages(long roomId, int limit, int offset) {
        Optional<PublicRoom> publicRoomOptional = DbUtils.findEntityById(PublicRoom.class, roomId);
        if (!publicRoomOptional.isPresent()) {
            return DbUtils.getNotFoundResult(PublicRoom.class, roomId);
        }

        return MessagesController.getMessages(roomId, limit, offset);
    }
}