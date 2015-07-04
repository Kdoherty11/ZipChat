package daos.impl;

import daos.AnonUserDao;
import models.entities.AnonUser;

/**
 * Created by kdoherty on 7/4/15.
 */
public class AnonUserDaoImpl extends GenericDaoImpl<AnonUser> implements AnonUserDao {

    public AnonUserDaoImpl() {
        super(AnonUser.class);
    }
}
