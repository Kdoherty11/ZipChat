package services;

import com.google.inject.ImplementedBy;
import daos.AnonUserDao;
import models.entities.AnonUser;
import models.entities.PublicRoom;
import models.entities.User;
import services.impl.AnonUserServiceImpl;

/**
 * Created by kdoherty on 7/4/15.
 */
@ImplementedBy(AnonUserServiceImpl.class)
public interface AnonUserService extends AnonUserDao {

    AnonUser getOrCreateAnonUser(User actual, PublicRoom room);

}
