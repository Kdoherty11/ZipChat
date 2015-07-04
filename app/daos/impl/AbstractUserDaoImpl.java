package daos.impl;

import models.entities.AbstractUser;
import daos.AbstractUserDao;

/**
 * Created by kdoherty on 6/30/15.
 */
public class AbstractUserDaoImpl extends GenericDaoImpl<AbstractUser> implements AbstractUserDao {

    public AbstractUserDaoImpl() {
        super(AbstractUser.class);
    }
}
