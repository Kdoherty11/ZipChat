package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.plugin.RedisPlugin;
import javafx.util.Callback;
import models.Room;
import play.data.Form;
import play.db.ebean.Model;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import views.html.index;


import java.util.List;

import static play.libs.Json.toJson;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static WebSocket<String> chat() {
        return new WebSocket<String>() {
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {

                System.out.println("ready");
                ChatController.join("test", in, out);

                // When the socket is closed.
                in.onClose(() -> {
                    System.out.println("quit");
                });
            }

        };
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
