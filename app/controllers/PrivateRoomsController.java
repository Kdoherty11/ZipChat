package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.entities.PrivateRoom;
import models.entities.User;
import models.sockets.RoomSocket;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import security.Secured;
import services.MessageService;
import services.PrivateRoomService;
import services.SecurityService;

import java.util.Optional;

@Security.Authenticated(Secured.class)
public class PrivateRoomsController extends AbstractRoomController {

    private final PrivateRoomService privateRoomService;
    private final SecurityService securityService;

    @Inject
    public PrivateRoomsController(final PrivateRoomService privateRoomService, final MessageService messageService,
                                  final SecurityService securityService) {
        super(messageService);
        this.privateRoomService = privateRoomService;
        this.securityService = securityService;
    }

    @Transactional(readOnly = true)
    public Result getRoomsByUserId(long userId) {
        if (securityService.isUnauthorized(userId)) {
            return forbidden();
        }
        Logger.debug("Getting Private Rooms by userId: " + userId);
        return okJson(privateRoomService.findByUserId(userId));
    }

    @Transactional
    public Result leaveRoom(long roomId, long userId) {
        if (securityService.isUnauthorized(userId)) {
            return forbidden();
        }

        Optional<PrivateRoom> roomOptional = privateRoomService.findById(roomId);

        if (roomOptional.isPresent()) {
            PrivateRoom room = roomOptional.get();

            boolean removed = privateRoomService.removeUser(room, userId);

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
        Optional<Long> userIdOptional = securityService.getUserId(authToken);
        if (!userIdOptional.isPresent()) {
            return WebSocket.reject(entityNotFound(User.class, userId));
        }
        if (userIdOptional.get() != userId && Play.isProd()) {
            return WebSocket.reject(forbidden());
        }

        Optional<PrivateRoom> privateRoomOptional = JPA.withTransaction(() -> privateRoomService.findById(roomId));
        if (privateRoomOptional.isPresent()) {
            if (isForbidden(privateRoomOptional.get())) {
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
            if (isForbidden(privateRoomOptional.get())) {
                return forbidden();
            }
        } else {
            return entityNotFound(PrivateRoom.class, roomId);
        }

        return getMessagesHelper(roomId, limit, offset);
    }

    private boolean isForbidden(PrivateRoom room) {
        return !privateRoomService.isUserInRoom(room, securityService.getTokenUserId()) && Play.isProd();
    }
}
