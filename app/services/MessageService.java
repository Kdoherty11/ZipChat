package services;

import com.google.inject.ImplementedBy;
import models.entities.Message;
import models.entities.User;
import repositories.MessageRepository;
import services.impl.MessageServiceImpl;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(MessageServiceImpl.class)
public interface MessageService extends MessageRepository {


    boolean favorite(Message message, User user);
}
