package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.entities.PrivateRoom;
import models.entities.User;
import models.sockets.RoomSocket;
import play.Logger;
import play.Play;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import security.Secured;
import security.SecurityHelper;
import services.PrivateRoomService;

import java.util.Optional;

@Security.Authenticated(Secured.class)
public class PrivateRoomsController extends BaseController {

    private PrivateRoomService privateRoomService;
    private SecurityHelper securityHelper;
    private MessagesController messagesController;

    @Inject
    public PrivateRoomsController(final PrivateRoomService privateRoomService, final MessagesController messagesController,
                                  final SecurityHelper securityHelper) {
        this.privateRoomService = privateRoomService;
        this.messagesController = messagesController;
        this.securityHelper = securityHelper;
    }

    @Transactional(readOnly = true)
    public Result getRoomsByUserId(long userId) {
        if (isUnauthorized(userId)) {
            return forbidden();
        }
        Logger.debug("Getting Private Rooms by userId: " + userId);
        return okJson(privateRoomService.findByUserId(userId));
    }

    @Transactional
    public Result leaveRoom(long roomId, long userId) {
        if (isUnauthorized(userId)) {
            return forbidden();
        }

        Optional<PrivateRoom> roomOptional = privateRoomService.findById(roomId);

        if (roomOptional.isPresent()) {
            PrivateRoom room = roomOptional.get();
            boolean removed = room.removeUser(userId);

            if (removed) {
                return OK_RESULT;
            } else {
                return badRequestJson("Unable to remove user with ID " + userId + " from the room because they are not in it");
            }

        } else {
            return entityNotFound(PrivateRoom.class, roomId);
        }
    }

    @Transactional
    public WebSocket<JsonNode> joinRoom(final long roomId, final long userId, String authToken) throws Throwable {
        Optional<Long> userIdOptional = securityHelper.getUserId(authToken);
        if (!userIdOptional.isPresent()) {
            return WebSocket.reject(entityNotFound(User.class, userId));
        }
        if (userIdOptional.get() != userId && Play.isProd()) {
            return WebSocket.reject(forbidden());
        }

        Optional<PrivateRoom> privateRoomOptional = privateRoomService.findById(roomId);
        if (privateRoomOptional.isPresent()) {
            if (!privateRoomOptional.get().isUserInRoom(userId) && Play.isProd()) {
                return WebSocket.reject(forbidden());
            }
        } else {
            return WebSocket.reject(entityNotFound(PrivateRoom.class, roomId));
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

    @Transactional(readOnly = true)
    public Result getMessages(long roomId, int limit, int offset) {
        Optional<PrivateRoom> privateRoomOptional = privateRoomService.findById(roomId);
        if (privateRoomOptional.isPresent()) {
            if (!privateRoomOptional.get().isUserInRoom(getTokenUserId()) && Play.isProd()) {
                return forbidden();
            }
        } else {
            return entityNotFound(PrivateRoom.class, roomId);
        }

        return messagesController.getMessages(roomId, limit, offset);
    }
}
