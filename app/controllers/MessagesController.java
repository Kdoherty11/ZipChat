package controllers;

import models.entities.Message;
import models.entities.User;
import play.db.jpa.Transactional;
import play.mvc.Result;
import utils.DbUtils;
import validation.DataValidator;
import validation.FieldValidator;
import validation.Validators;

import java.util.Optional;

public class MessagesController extends BaseController {

    private static final boolean ADD_FAVORITE = true;
    private static final boolean REMOVE_FAVORITE = false;

    @Transactional
    public static Result getMessages(long roomId, int limit, int offset) {

        DataValidator validator = new DataValidator(
                new FieldValidator<>("roomId", roomId, Validators.min(1)),
                new FieldValidator<>("limit", limit, Validators.min(0)),
                new FieldValidator<>("offset", offset, Validators.min(0)));

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
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
                return DbUtils.getNotFoundResult(User.ENTITY_NAME, userId);
            }
        } else {
            return DbUtils.getNotFoundResult(Message.ENTITY_NAME, messageId);
        }
    }
}
