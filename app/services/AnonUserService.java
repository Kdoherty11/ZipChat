package services;

import com.google.inject.ImplementedBy;
import daos.AnonUserDao;
import models.entities.AnonUser;
import notifications.AbstractNotification;
import services.impl.AnonUserServiceImpl;

/**
 * Created by kdoherty on 7/4/15.
 */
@ImplementedBy(AnonUserServiceImpl.class)
public interface AnonUserService extends AnonUserDao {

    void sendNotification(AnonUser receiver, AbstractNotification notification);

}
