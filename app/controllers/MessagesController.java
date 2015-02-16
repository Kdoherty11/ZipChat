package controllers;

import models.entities.Message;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import static play.libs.F.Promise;
import static play.libs.Json.toJson;


public class MessagesController extends Controller {

    public static Promise<Result> getMessages(String roomId) {
        Logger.debug("Getting all messages in room " + roomId);

        Promise<List<Message>> promiseEntities = Promise.promise(() -> Message.find.where().ieq("roomId", roomId).findList());
        return promiseEntities.flatMap(entities -> Promise.promise(() -> ok(toJson(entities))));
    }

}
