package controllers;

import com.google.inject.Inject;
import models.Message;
import models.User;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import security.Secured;
import services.MessageService;
import services.SecurityService;
import services.UserService;

import java.util.Optional;

@Security.Authenticated(Secured.class)
public class MessagesController extends BaseController {

    private final MessageService messageService;
    private final UserService userService;
    private final SecurityService securityService;

    @Inject
    public MessagesController(final MessageService messageService, final UserService userService, final SecurityService securityService) {
        this.messageService = messageService;
        this.userService = userService;
        this.securityService = securityService;
    }

    @Transactional
    public Result favorite(long messageId, long userId) {
        return messageActionHelper(messageId, userId, new UserMessageAction() {
            @Override
            public boolean messageAction(Message message, User user) {
                return messageService.favorite(message, user);
            }

            @Override
            public Result onActionFailed(User user) {
                return badRequest("User " + userId + " has already favorited this message");
            }
        });
    }

    @Transactional
    public Result removeFavorite(long messageId, long userId) {
        return messageActionHelper(messageId, userId, new UserMessageAction() {
            @Override
            public boolean messageAction(Message message, User user) {
                return messageService.removeFavorite(message, user);
            }

            @Override
            public Result onActionFailed(User user) {
                return notFound("User " + userId + " has not favorited this message");
            }
        });
    }

    @Transactional
    public Result flag(long messageId, long userId) {
        return messageActionHelper(messageId, userId, new UserMessageAction() {
            @Override
            public boolean messageAction(Message message, User user) {
                return messageService.flag(message, user);
            }

            @Override
            public Result onActionFailed(User user) {
                return badRequest("User " + user.userId + " has already flagged this message");
            }
        });
    }

    @Transactional
    public Result removeFlag(long messageId, long userId) {
        return messageActionHelper(messageId, userId, new UserMessageAction() {
            @Override
            public boolean messageAction(Message message, User user) {
                return messageService.removeFlag(message, user);
            }

            @Override
            public Result onActionFailed(User user) {
                return notFound("User " + user.userId + " has not flagged this message");
            }
        });
    }

    // Public due to http://stackoverflow.com/a/21442580/3258892
    public interface UserMessageAction {
        boolean messageAction(Message message, User user);
        Result onActionFailed(User user);
    }

    private Result messageActionHelper(long messageId, long userId, UserMessageAction cb) {
        if (securityService.isUnauthorized(userId)) {
            return Results.forbidden();
        }
        Optional<Message> messageOptional = messageService.findById(messageId);

        if (messageOptional.isPresent()) {

            Optional<User> userOptional = userService.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                boolean success = cb.messageAction(messageOptional.get(), user);
                if (!success) {
                    return cb.onActionFailed(user);
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
