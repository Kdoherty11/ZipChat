package controllers;

import models.entities.AbstractRoom;
import models.entities.Message;
import models.entities.PrivateRoom;
import models.entities.User;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import security.Secured;
import utils.DbUtils;
import validation.DataValidator;
import validation.FieldValidator;
import validation.validators.Validators;

import java.util.Optional;

@Security.Authenticated(Secured.class)
public class MessagesController extends BaseController {

    private interface UserMessageAction {
        boolean messageAction(Message message, User user);
        String onActionFailed(User user);
    }

    @Transactional
    public static Result getMessages(long roomId, int limit, int offset) {

        Optional<AbstractRoom> roomOptional = DbUtils.findEntityById(AbstractRoom.class, roomId);
        if (roomOptional.isPresent()) {
            AbstractRoom room = roomOptional.get();

            if (room instanceof PrivateRoom) {
                if (!((PrivateRoom) room).isUserInRoom(getTokenUserId())) {
                    return forbidden();
                }
            }
        } else {
            return DbUtils.getNotFoundResult(AbstractRoom.class, roomId);
        }

        DataValidator validator = new DataValidator(
                new FieldValidator<>("limit", limit, Validators.min(0)),
                new FieldValidator<>("offset", offset, Validators.min(0)));

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        return okJson(Message.getMessages(roomId, limit, offset));
    }

    @Transactional
    public static Result favorite(long messageId, long userId) {
        return messageActionHelper(messageId, userId, new UserMessageAction() {
            @Override
            public boolean messageAction(Message message, User user) {
                return message.favorite(user);
            }

            @Override
            public String onActionFailed(User user) {
                return "User " + userId + " has already favorited this message";
            }
        });
    }

    @Transactional
    public static Result removeFavorite(long messageId, long userId) {
        return messageActionHelper(messageId, userId, new UserMessageAction() {
            @Override
            public boolean messageAction(Message message, User user) {
                return message.removeFavorite(user);
            }

            @Override
            public String onActionFailed(User user) {
                return "User " + userId + " has not favorited this message";
            }
        });
    }

    @Transactional
    public static Result flag(long messageId, long userId) {
        return messageActionHelper(messageId, userId, new UserMessageAction() {
            @Override
            public boolean messageAction(Message message, User user) {
                return message.flag(user);
            }

            @Override
            public String onActionFailed(User user) {
                return "User " + user.userId + " has already flagged this message";
            }
        });
    }

    @Transactional
    public static Result removeFlag(long messageId, long userId) {
        return messageActionHelper(messageId, userId, new UserMessageAction() {
            @Override
            public boolean messageAction(Message message, User user) {
                return message.removeFlag(user);
            }

            @Override
            public String onActionFailed(User user) {
                return "User " + user.userId + " has not flagged this message";
            }
        });
    }

    public static Result messageActionHelper(long messageId, long userId, UserMessageAction cb) {
        if (isUnauthorized(userId)) {
            return Results.forbidden();
        }
        Optional<Message> messageOptional = DbUtils.findEntityById(Message.class, messageId);

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();

            Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                boolean success = cb.messageAction(message, user);
                if (!success) {
                    return badRequestJson(cb.onActionFailed(user));
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
