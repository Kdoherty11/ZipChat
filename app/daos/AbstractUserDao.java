package daos;

import com.google.inject.ImplementedBy;
import daos.impl.AbstractUserDaoImpl;
import models.AbstractUser;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(AbstractUserDaoImpl.class)
public interface AbstractUserDao extends GenericDao<AbstractUser> {
}
