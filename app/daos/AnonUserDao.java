package daos;

import com.google.inject.ImplementedBy;
import daos.impl.AnonUserDaoImpl;
import models.entities.AnonUser;

/**
 * Created by kdoherty on 7/4/15.
 */
@ImplementedBy(AnonUserDaoImpl.class)
public interface AnonUserDao extends GenericDao<AnonUser> {

}
