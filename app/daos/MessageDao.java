package daos;

import com.google.inject.ImplementedBy;
import daos.impl.MessageDaoImpl;
import models.entities.Message;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(MessageDaoImpl.class)
public interface MessageDao extends GenericDao<Message> {
    Optional<Message> findById(long messageId);
    List<Message> getMessages(long roomId, int limit, int offset);
}
