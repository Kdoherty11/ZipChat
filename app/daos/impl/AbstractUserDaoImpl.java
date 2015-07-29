package daos.impl;

import daos.AbstractUserDao;
import models.entities.AbstractUser;

/**
 * Created by kdoherty on 6/30/15.
 */
public class AbstractUserDaoImpl extends GenericDaoImpl<AbstractUser> implements AbstractUserDao {

    public AbstractUserDaoImpl() {
        super(AbstractUser.class);
    }
}
