package controllers;

import models.entities.Message;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Result;


public class MessagesController extends BaseController {

    @Transactional
    public static Result getMessages(String roomId) {
        Logger.debug("Getting all messages in room " + roomId);
        return okJson(Message.getByRoomId(roomId));
    }

    @Transactional
    public static Result createMessage(String roomId) {
        return create(Message.class);
    }

}
