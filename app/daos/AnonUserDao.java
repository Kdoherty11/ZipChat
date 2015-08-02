package daos;

import com.google.inject.ImplementedBy;
import daos.impl.AnonUserDaoImpl;
import models.AnonUser;
import models.PublicRoom;
import models.User;

import java.util.Optional;

/**
 * Created by kdoherty on 7/4/15.
 */
@ImplementedBy(AnonUserDaoImpl.class)
public interface AnonUserDao extends GenericDao<AnonUser> {

    Optional<AnonUser> getAnonUser(User actual, PublicRoom room);

}
