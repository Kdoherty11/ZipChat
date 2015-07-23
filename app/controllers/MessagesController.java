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

    // Public due to http://stackoverflow.com/a/21442580/3258892
    public interface UserMessageAction {
        boolean messageAction(Message message, User user);
        String onActionFailed(User user);
    }

    @Transactional
    public Result favorite(long messageId, long userId) {
        return messageActionHelper(messageId, userId, new UserMessageAction() {
            @Override
            public boolean messageAction(Message message, User user) {
                return messageService.favorite(message, user);
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
                return messageService.removeFavorite(message, user);
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
                return messageService.flag(message, user);
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
                return messageService.removeFlag(message, user);
            }

            @Override
            public String onActionFailed(User user) {
                return "User " + user.userId + " has not flagged this message";
            }
        });
    }

    public Result messageActionHelper(long messageId, long userId, UserMessageAction cb) {
        if (securityService.isUnauthorized(userId)) {
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
                    return notFound(cb.onActionFailed(user));
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
