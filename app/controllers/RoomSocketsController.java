package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.entities.PrivateRoom;
import models.entities.User;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Security;
import play.mvc.WebSocket;
import security.Secured;
import services.PrivateRoomService;
import services.RoomSocketService;
import services.SecurityService;

import java.util.Optional;

/**
 * Created by kdoherty on 7/13/15.
 */
@Security.Authenticated(Secured.class)
public class RoomSocketsController extends BaseController {

    private final SecurityService securityService;

    private final PrivateRoomService privateRoomService;

    private final RoomSocketService roomSocketService;

    @Inject
    public RoomSocketsController(final SecurityService securityService, final PrivateRoomService privateRoomService,
                                 final RoomSocketService roomSocketService) {
        this.securityService = securityService;
        this.privateRoomService = privateRoomService;
        this.roomSocketService = roomSocketService;
    }

    @Transactional
    public WebSocket<JsonNode> joinPublicRoom(final long roomId, final long userId, String authToken) {
        Optional<Long> userIdOptional = securityService.getUserId(authToken);
        if ((!userIdOptional.isPresent() || userIdOptional.get() != userId) && Play.isProd()) {
            return WebSocket.reject(forbidden());
        }

        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                try {
                    roomSocketService.join(roomId, userId, in, out);
                } catch (Exception ex) {
                    Logger.error("Problem joining the RoomSocket", ex.getMessage());
                }
            }
        };
    }

    @Transactional
    public WebSocket<JsonNode> joinPrivateRoom(final long roomId, final long userId, String authToken) throws Throwable {
        Optional<Long> userIdOptional = securityService.getUserId(authToken);
        if (!userIdOptional.isPresent()) {
            return WebSocket.reject(entityNotFound(User.class, userId));
        }
        if (userIdOptional.get() != userId && Play.isProd()) {
            return WebSocket.reject(forbidden());
        }

        Optional<PrivateRoom> privateRoomOptional = JPA.withTransaction(() -> privateRoomService.findById(roomId));
        if (privateRoomOptional.isPresent()) {
            if (securityService.isUnauthorized(privateRoomOptional.get())) {
                return WebSocket.reject(forbidden());
            }
        } else {
            return WebSocket.reject(entityNotFound(PrivateRoom.class, roomId));
        }

        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                try {
                    roomSocketService.join(roomId, userId, in, out);
                } catch (Exception ex) {
                    Logger.error("Problem joining the RoomSocket: " + ex.getMessage());
                }
            }
        };
    }


}
