package services.impl;

import com.google.inject.Inject;
import models.entities.Message;
import models.entities.User;
import notifications.MessageFavoritedNotification;
import play.Logger;
import repositories.MessageRepository;
import services.MessageService;

import java.util.List;

/**
 * Created by kdoherty on 7/3/15.
 */
public class MessageServiceImpl extends GenericServiceImpl<Message> implements MessageService {
    private MessageRepository messageRepository;

    @Inject
    public MessageServiceImpl(final MessageRepository messageRepository) {
        super(messageRepository);
        this.messageRepository = messageRepository;
    }

    @Override
    public List<Message> getMessages(long roomId, int limit, int offset) {
        return messageRepository.getMessages(roomId, limit, offset);
    }

    @Override
    public boolean favorite(Message message, User user) {
        if (message.favorites.contains(user)) {
            Logger.error(user + " is attempting to favorite " + this + " but has already favorited it");
            return false;
        }
        message.favorites.add(user);
        message.score++;
        User actual = message.sender.getActual();
        if (!user.equals(actual)) {
            actual.sendNotification(new MessageFavoritedNotification(message, user));
        }
        return true;
    }
}
