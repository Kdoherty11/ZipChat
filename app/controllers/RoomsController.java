package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Room;
import models.RoomSocket;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.WebSocket;


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
    public static Result updateRoom(long id) {
        return update(Room.class, id);
    }

    @Transactional
    public static Result showRoom(long id) {
        return show(Room.class, id);
    }

    @Transactional
    public static Result deleteRoom(long id) {
        return delete(Room.class, id);
    }

    @Transactional
    public static Result getGeoRooms(double lat, double lon) {
        return okJson(Room.allInGeoRange(lat, lon));
    }
}
