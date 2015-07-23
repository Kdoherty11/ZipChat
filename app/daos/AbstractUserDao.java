package daos;

import com.google.inject.ImplementedBy;
import models.entities.AbstractUser;
import daos.impl.AbstractUserDaoImpl;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(AbstractUserDaoImpl.class)
public interface AbstractUserDao extends GenericDao<AbstractUser> {
}
