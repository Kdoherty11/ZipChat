package services;

import com.google.inject.ImplementedBy;
import daos.AnonUserDao;
import models.AnonUser;
import models.PublicRoom;
import models.User;
import services.impl.AnonUserServiceImpl;

/**
 * Created by kdoherty on 7/4/15.
 */
@ImplementedBy(AnonUserServiceImpl.class)
public interface AnonUserService extends AnonUserDao {

    AnonUser getOrCreateAnonUser(User actual, PublicRoom room);
}
