package services;

import com.google.inject.Inject;
import models.entities.Message;
import repositories.MessageRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 7/1/15.
 */
public class MessageService implements MessageRepository {

    private MessageRepository messageRepository;

    @Inject
    public MessageService(final MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Optional<Message> findById(long messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    public List<Message> getMessages(long roomId, int limit, int offset) {
        return messageRepository.getMessages(roomId, limit, offset);
    }
}
