package repositories;

import com.google.inject.ImplementedBy;
import models.entities.Message;
import repositories.impl.MessageRepositoryImpl;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(MessageRepositoryImpl.class)
public interface MessageRepository {
    public Optional<Message> findById(long messageId);
    public List<Message> getMessages(long roomId, int limit, int offset);
}
