package controllers;

import play.*;
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
        Room room = new Room();
        room.name = "Test Room";
        room.save();
        return ok();
    }

    public static Result getRooms() {
        List<Room> tasks = new Model.Finder(String.class, Room.class).all();
        return ok(toJson(tasks));
    }

}
