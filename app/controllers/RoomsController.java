package controllers;

import models.ChatRoom;
import models.ChatRoomModel;
import play.Logger;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import static play.libs.Json.toJson;

/**
 * Created by zacharywebert on 2/11/15.
 */
public class RoomsController extends Controller {
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
