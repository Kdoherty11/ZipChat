package controllers;

import com.google.inject.Inject;
import models.entities.Message;
import models.entities.User;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import security.Secured;
import services.MessageService;
import services.UserService;
import validation.DataValidator;
import validation.FieldValidator;
import validation.validators.Validators;

import java.util.Optional;

@Security.Authenticated(Secured.class)
public class MessagesController extends BaseController {

    private final MessageService messageService;
    private final UserService userService;

    @Inject
    public MessagesController(final MessageService messageService, final UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    private interface UserMessageAction {
        boolean messageAction(Message message, User user);
        String onActionFailed(User user);
    }

    public Result getMessages(long roomId, int limit, int offset) {
        DataValidator validator = new DataValidator(
                new FieldValidator<>("limit", limit, Validators.min(0)),
                new FieldValidator<>("offset", offset, Validators.min(0)));

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        return okJson(messageService.getMessages(roomId, limit, offset));
    }

    @Transactional
    public Result favorite(long messageId, long userId) {
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
    public Result removeFavorite(long messageId, long userId) {
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
    public Result flag(long messageId, long userId) {
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
    public Result removeFlag(long messageId, long userId) {
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

    public Result messageActionHelper(long messageId, long userId, UserMessageAction cb) {
        if (isUnauthorized(userId)) {
            return Results.forbidden();
        }
        Optional<Message> messageOptional = messageService.findById(messageId);

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();

            Optional<User> userOptional = userService.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                boolean success = cb.messageAction(message, user);
                if (!success) {
                    return badRequestJson(cb.onActionFailed(user));
                }
                return OK_RESULT;
            } else {
                return entityNotFound(User.class, userId);
            }
        } else {
            return entityNotFound(Message.class, messageId);
        }
    }
}
