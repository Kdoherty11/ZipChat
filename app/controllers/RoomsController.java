package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.RoomSocket;
import models.entities.Room;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import static play.data.Form.form;
import static play.libs.F.Promise;
import static play.libs.Json.toJson;


public class RoomsController extends Controller {

    private static final CrudUtils.Callback DEFAULT_CB = new CrudUtils.Callback() {
        @Override
        public Result success(JsonNode entity) {
            return ok(entity);
        }

        @Override
        public Result failure(JsonNode error) {
            return badRequest(error);
        }
    };

    /**
     * Handle the chat websocket.
     */
    public static WebSocket<JsonNode> joinRoom(final String roomId, final String username) {


        return new WebSocket<JsonNode>() {
            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){

                Logger.debug("join" + roomId + " " + username);
                // Join the chat room.
                try {
                    RoomSocket.join(roomId, username, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public static Promise<Result> createRoom() {
        return CrudUtils.create(form(Room.class).bindFromRequest(), DEFAULT_CB);
    }

    public static Promise<Result> getRooms() {
        return CrudUtils.read(Room.class, entities -> ok(entities));
    }

    public static Promise<Result> getGeoRooms(double lat, double lng) {
        return Promise.promise(() -> ok(toJson(Room.allInGeoRange(lat, lng))));
    }

    public static Promise<Result> updateRoom(String id) {
        return CrudUtils.update(id, Room.class, form().bindFromRequest(), DEFAULT_CB);
    }

    public static Promise<Result> getRoom(String id) {
        return CrudUtils.show(id, Room.class, DEFAULT_CB);
    }

    public static Promise<Result> deleteRoom(String id) {
        return CrudUtils.delete(id, Room.class, DEFAULT_CB);
    }
}
