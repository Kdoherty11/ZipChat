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

    private final RoomSocketService roomSocketService;

    private final PrivateRoomService privateRoomService;

    private final SecurityService securityService;

    @Inject
    public RoomSocketsController(final RoomSocketService roomSocketService, final PrivateRoomService privateRoomService,
                                 final SecurityService securityService) {
        this.roomSocketService = roomSocketService;
        this.privateRoomService = privateRoomService;
        this.securityService = securityService;
    }

    @Transactional
    public WebSocket<JsonNode> joinPublicRoom(final long roomId, final long userId, String authToken) {
        if (securityService.isUnauthorized(authToken, userId)) {
            return WebSocket.reject(forbidden());
        }

        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                try {
                    roomSocketService.join(roomId, userId, in, out);
                } catch (Exception ex) {
                    Logger.error("Problem joining the RoomSocket", ex);
                }
            }
        };
    }

    @Transactional
    public WebSocket<JsonNode> joinPrivateRoom(final long roomId, final long userId, String authToken) throws Throwable {
        if (securityService.isUnauthorized(authToken, userId)) {
            return WebSocket.reject(forbidden());
        }

        Optional<PrivateRoom> privateRoomOptional = JPA.withTransaction(() -> privateRoomService.findById(roomId));
        if (privateRoomOptional.isPresent()) {
            if (securityService.isUnauthorized(authToken, privateRoomOptional.get())) {
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
                    Logger.error("Problem joining the RoomSocket", ex);
                }
            }
        };
    }


}
