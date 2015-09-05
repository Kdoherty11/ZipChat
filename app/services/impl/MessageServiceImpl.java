package services.impl;

import com.google.inject.Inject;
import daos.MessageDao;
import models.Message;
import models.PrivateRoom;
import models.User;
import notifications.MessageFavoritedNotification;
import services.MessageService;
import services.UserService;

import java.util.List;

/**
 * Created by kdoherty on 7/3/15.
 */
public class MessageServiceImpl extends GenericServiceImpl<Message> implements MessageService {
    private MessageDao messageDao;
    private UserService userService;

    @Inject
    public MessageServiceImpl(final MessageDao messageDao, final UserService userService) {
        super(messageDao);
        this.messageDao = messageDao;
        this.userService = userService;
    }

    @Override
    public boolean favorite(Message message, User user) {
        boolean didFavorite = message.favorites.add(user);
        if (didFavorite) {
            message.score++;
            User actual = message.sender.getActual();
            boolean shouldSendNotification = !user.equals(actual);
            if (message.room instanceof PrivateRoom) {
                PrivateRoom room = (PrivateRoom) message.room;
                boolean otherUserHasNotLeft = user.equals(room.sender) ? room.receiverInRoom : room.senderInRoom;
                shouldSendNotification &= otherUserHasNotLeft;
            }

            if (shouldSendNotification) {
                userService.sendNotification(actual, new MessageFavoritedNotification(message, user));
            }
        }

        return didFavorite;
    }

    @Override
    public boolean removeFavorite(Message message, User user) {
        boolean didRemoveFavorite = message.favorites.remove(user);
        if (didRemoveFavorite) {
            message.score--;
        }
        return didRemoveFavorite;
    }

    @Override
    public boolean flag(Message message, User user) {
        return message.flags.add(user);
    }

    @Override
    public boolean removeFlag(Message message, User user) {
        return message.flags.remove(user);
    }

    @Override
    public List<Message> getMessages(long roomId, int limit, int offset) {
        return messageDao.getMessages(roomId, limit, offset);
    }
}
