package services.impl;

import daos.AnonUserDao;
import models.entities.AnonUser;
import services.AnonUserService;

/**
 * Created by kdoherty on 7/4/15.
 */
public class AnonUserServiceImpl extends GenericServiceImpl<AnonUser> implements AnonUserService {

    public AnonUserServiceImpl(final AnonUserDao anonUserDao) {
        super(anonUserDao);
    }

}
