package controllers;

import models.Message;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Result;


public class MessagesController extends BaseController {

    @Transactional
    public static Result getMessages(long roomId) {
        Logger.debug("Getting all messages in room " + roomId);
        return okJson(Message.getByRoomId(roomId));
    }

    @Transactional
    public static Result createMessage(long roomId) {
        return create(Message.class);
    }

}
