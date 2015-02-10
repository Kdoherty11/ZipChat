package controllers;


import com.typesafe.plugin.RedisPlugin;
import models.Room;
import play.Logger;
import play.data.Form;
import play.db.ebean.Model;
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

        System.out.println("Chat endpoint");

        return new WebSocket<String>() {
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {

                System.out.println("ready");
                ChatController.join("test", in, out);

                in.onMessage(json -> System.out.println(json));

                // When the socket is closed.
                in.onClose(() -> System.out.println("quit"));
            }

        };
    }

    public static Result  test() {
        System.out.println("I am here");
        Logger.debug("Debug log");

        try {

            play.Application app = play.Play.application();
            Logger.debug("app: " + app);
            Object plugin = app.plugin(RedisPlugin.class);
            Logger.debug("plugin: " + plugin);

            Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();

            Logger.info("Success init Jedis!!!!");
        } catch (Exception e) {
            Logger.error("Jedis init error: " + e);
        }


        return ok(toJson("Ok Json"));

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
