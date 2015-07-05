package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.ImplementedBy;
import models.entities.AbstractUser;
import models.entities.User;
import notifications.AbstractNotification;
import daos.UserDao;
import services.impl.UserServiceImpl;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(UserServiceImpl.class)
public interface UserService extends UserDao {

    void sendChatRequest(User sender, AbstractUser receiver);
    void sendNotification(User receiver, AbstractNotification notification);
    JsonNode getFacebookInformation(String fbAccessToken);
}