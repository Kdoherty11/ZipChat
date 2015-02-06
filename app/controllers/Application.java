package controllers;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Akka;
import play.libs.F;
import play.mvc.*;
import models.Room;
import play.db.ebean.Model;
import java.util.List;

import static play.libs.Json.toJson;

import scala.concurrent.duration.Duration;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static WebSocket<String> chat() {
        return new WebSocket<String>() {
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                in.onMessage(new F.Callback<String>() {
                    @Override
                    public void invoke(String s) throws Throwable {
                        System.out.println(s);
                    }
                });

                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        System.out.println("Closed");
                    }
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
