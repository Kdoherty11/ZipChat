package daos;

import com.google.inject.ImplementedBy;
import daos.impl.AnonUserDaoImpl;
import models.entities.AnonUser;
import models.entities.PublicRoom;
import models.entities.User;

import java.util.Optional;

/**
 * Created by kdoherty on 7/4/15.
 */
@ImplementedBy(AnonUserDaoImpl.class)
public interface AnonUserDao extends GenericDao<AnonUser> {

    Optional<AnonUser> getAnonUser(User actual, PublicRoom room);

}
