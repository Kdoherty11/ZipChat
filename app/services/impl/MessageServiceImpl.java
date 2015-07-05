package services.impl;

import com.google.inject.Inject;
import daos.MessageDao;
import models.entities.Message;
import models.entities.User;
import notifications.MessageFavoritedNotification;
import play.Logger;
import services.AbstractUserService;
import services.MessageService;

import java.util.List;

/**
 * Created by kdoherty on 7/3/15.
 */
public class MessageServiceImpl extends GenericServiceImpl<Message> implements MessageService {
    private MessageDao messageDao;
    private AbstractUserService abstractUserService;

    @Inject
    public MessageServiceImpl(final MessageDao messageDao, final AbstractUserService abstractUserService) {
        super(messageDao);
        this.messageDao = messageDao;
        this.abstractUserService = abstractUserService;
    }

    @Override
    public List<Message> getMessages(long roomId, int limit, int offset) {
        return messageDao.getMessages(roomId, limit, offset);
    }

    @Override
    public boolean favorite(Message message, User user) {
        if (message.favorites.contains(user)) {
            Logger.error(user + " is attempting to favorite " + message + " but has already favorited it");
            return false;
        }
        message.favorites.add(user);
        message.score++;
        User actual = message.sender.getActual();
        if (!user.equals(actual)) {
            abstractUserService.sendNotification(actual, new MessageFavoritedNotification(message, user));
        }
        return true;
    }

    @Override
    public boolean removeFavorite(Message message, User user) {
        boolean didRemoveFavorite = message.favorites.remove(user);
        if (didRemoveFavorite) {
            message.score--;
        } else {
            Logger.warn(user + " attempted to remove favorite from " + message + " but has not favorited it");
        }
        return didRemoveFavorite;
    }

    @Override
    public boolean flag(Message message, User user) {
        if (message.flags.contains(user)) {
            Logger.error(user + " is attempting to flag " + message + " but has already flagged it");
            return false;
        }

        message.flags.add(user);
        return true;
    }

    @Override
    public boolean removeFlag(Message message, User user) {
        boolean didRemoveFlag = message.flags.remove(user);
        if (!didRemoveFlag) {
            Logger.warn(user + " attempted to remove a flag from " + message + " but has not flagged it");
        }
        return didRemoveFlag;
    }
}
