package controllers;

import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import models.Room;
import play.db.ebean.Model;
import java.util.List;

import static play.libs.Json.toJson;

import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result createRoom() {
        Form<Room> roomForm = Form.form(Room.class).bindFromRequest();
        if (roomForm.hasErrors()) {
            return badRequest(roomForm.errorsAsJson());
        } else {
            Room room = roomForm.get();
            room.save();
            return ok(toJson(room));
        }
    }

    public static Result getRooms() {
        List<Room> tasks = new Model.Finder(String.class, Room.class).all();
        return ok(toJson(tasks));
    }

}
