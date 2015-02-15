package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.entities.Message;
import models.entities.Room;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import static play.data.Form.form;
import static play.libs.F.Promise;
import static play.libs.Json.toJson;


public class MessagesController extends Controller {

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

    public static Promise<Result> createMessage(String roomId) {
        Logger.debug("Creating a message");

        Form<Message> form = form(Message.class).bindFromRequest();

        if (form.hasErrors()) {
            return Promise.promise(() -> badRequest(form.errorsAsJson()));
        } else {
            Message message = form.get();
            Room room = Room.find.byId(roomId);
            if (room == null) {
                return Promise.promise(() -> badRequest(CrudUtils.buildEntityNotFoundError("Room", roomId)));
            }
            message.room = room;
            message.save();
            return Promise.promise(() -> ok(toJson(message)));
        }
    }

    public static Promise<Result> getMessages(String roomId) {
        Logger.debug("Getting all messages with roomId " + roomId);

        Promise<List<Message>> promiseEntities = Promise.promise(() -> Message.find.where().ieq("roomId", roomId).findList());
        return promiseEntities.flatMap(entities -> Promise.promise(() -> ok(toJson(entities))));
    }

    public static Promise<Result> updateMessage(String roomId, String msgId) {
        return CrudUtils.update(msgId, Message.class, form().bindFromRequest(), DEFAULT_CB);
    }

    public static Promise<Result> getMessage(String roomId, String msgId) {
        return CrudUtils.show(msgId, Message.class, DEFAULT_CB);
    }

    public static Promise<Result> deleteMessage(String roomId, String msgId) {
        return CrudUtils.delete(msgId, Message.class, DEFAULT_CB);
    }
}
