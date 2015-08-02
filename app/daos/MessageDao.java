package daos;

import com.google.inject.ImplementedBy;
import daos.impl.MessageDaoImpl;
import models.Message;

import java.util.List;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(MessageDaoImpl.class)
public interface MessageDao extends GenericDao<Message> {
    List<Message> getMessages(long roomId, int limit, int offset);
}
