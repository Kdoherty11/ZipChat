package controllers;

import models.entities.Message;
import models.entities.User;
import play.db.jpa.Transactional;
import play.mvc.Result;
import utils.DbUtils;

import java.util.Optional;

public class MessagesController extends BaseController {

    private static final boolean ADD_FAVORITE = true;
    private static final boolean REMOVE_FAVORITE = false;

    @Transactional
    public static Result getMessages(long roomId, int limit, int offset) {
        if (roomId < 1) {
            return badRequestJson("roomId must be positive");
        }
        if (limit < 0) {
            return badRequestJson("offset must be at least 0");
        }
        if (offset < 0) {
            return badRequestJson("offset must be at least 0");
        }

        return okJson(Message.getMessages(roomId, limit, offset));
    }

    @Transactional
    public static Result favorite(long messageId, long userId) {
        return favorite(messageId, userId, ADD_FAVORITE);
    }

    @Transactional
    public static Result removeFavorite(long messageId, long userId) {
        return favorite(messageId, userId, REMOVE_FAVORITE);
    }

    public static Result favorite(long messageId, long userId, boolean isAddFavorite) {
        Optional<Message> messageOptional = DbUtils.findEntityById(Message.class, messageId);

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();

            Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);
            if (userOptional.isPresent()) {
                if (isAddFavorite) {
                    message.favorite(userOptional.get());
                } else {
                    message.removeFavorite(userOptional.get());
                }
                return OK_RESULT;
            } else {
                return badRequestJson(DbUtils.buildEntityNotFoundError("User", userId));
            }
        } else {
            return badRequestJson(DbUtils.buildEntityNotFoundError("Message", messageId));
        }
    }
}
