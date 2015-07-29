package services;

import com.google.inject.ImplementedBy;
import daos.MessageDao;
import models.entities.Message;
import models.entities.User;
import services.impl.MessageServiceImpl;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(MessageServiceImpl.class)
public interface MessageService extends MessageDao {

    boolean favorite(Message message, User user);
    boolean removeFavorite(Message message, User user);
    boolean flag(Message message, User user);
    boolean removeFlag(Message message, User user);
}
