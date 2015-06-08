package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.entities.PrivateRoom;
import models.sockets.RoomSocket;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import security.Secured;
import security.SecurityHelper;
import utils.DbUtils;

import java.util.Optional;

@Security.Authenticated(Secured.class)
public class PrivateRoomsController extends BaseController {

    @Transactional
    public static Result getRoomsByUserId(long userId) {
        if (isUnauthorized(userId)) {
            return forbidden();
        }
        Logger.debug("Getting Private Rooms by userId: " + userId);
        return okJson(PrivateRoom.getRoomsByUserId(userId));
    }

    @Transactional
    public static Result leaveRoom(long roomId, long userId) {
        if (isUnauthorized(userId)) {
            return forbidden();
        }

        Optional<PrivateRoom> roomOptional = DbUtils.findEntityById(PrivateRoom.class, roomId);

        if (roomOptional.isPresent()) {
            PrivateRoom room = roomOptional.get();
            boolean removed = room.removeUser(userId);

            if (removed) {
                return OK_RESULT;
            } else {
                return badRequestJson("Unable to remove user with ID " + userId + " from the room because they are not in it");
            }

        } else {
            return DbUtils.getNotFoundResult(PrivateRoom.ENTITY_NAME, roomId);
        }
    }

    @Transactional
    public static WebSocket<JsonNode> joinRoom(final long roomId, final long userId, String authToken) {
        Optional<Long> userIdOptional = SecurityHelper.getUserId(authToken);
        if (!userIdOptional.isPresent() || userIdOptional.get() != userId) {
            return WebSocket.reject(forbidden());
        }

        Optional<PrivateRoom> privateRoomOptional = DbUtils.findEntityById(PrivateRoom.class, roomId);
        if (privateRoomOptional.isPresent()) {
            if (!privateRoomOptional.get().isUserInRoom(userId)) {
                return WebSocket.reject(forbidden());
            }
        } else {
            return WebSocket.reject(DbUtils.getNotFoundResult(PrivateRoom.class, roomId));
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
        Optional<PrivateRoom> privateRoomOptional = DbUtils.findEntityById(PrivateRoom.class, roomId);
        if (privateRoomOptional.isPresent()) {
            if (!privateRoomOptional.get().isUserInRoom(getTokenUserId())) {
                return forbidden();
            }
        } else {
            return DbUtils.getNotFoundResult(PrivateRoom.class, roomId);
        }

        return MessagesController.getMessages(roomId, limit, offset);
    }
}
