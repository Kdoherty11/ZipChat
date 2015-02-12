package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.ChatRoom;
import models.ChatRoomModel;
import play.Logger;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.util.List;

import static play.libs.Json.toJson;

/**
 * Created by kevindoherty on 2/11/15.
 */
public class ChatRoomsController extends Controller {

    /**
     * Handle the chat websocket.
     */
    public static WebSocket<JsonNode> joinRoom(final String roomId, final String username) {
        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){

                // Join the chat room.
                try {
                    ChatRoom.join(roomId, username, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public static Result createRoom() {
        Logger.debug("Received create rooms request");

        Form<ChatRoomModel> roomForm = Form.form(ChatRoomModel.class).bindFromRequest();
        if (roomForm.hasErrors()) {
            return badRequest(roomForm.errorsAsJson());
        } else {
            ChatRoomModel chatRoom = roomForm.get();
            chatRoom.save();
            return ok(toJson(chatRoom));
        }
    }

    public static Result getRooms() {
        Logger.debug("Received get rooms request");
        List<ChatRoom> tasks = new Model.Finder(String.class, ChatRoomModel.class).all();
        return ok(toJson(tasks));
    }

}
