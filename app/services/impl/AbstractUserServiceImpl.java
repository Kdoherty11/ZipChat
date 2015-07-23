package services.impl;

import com.google.inject.Inject;
import daos.AbstractUserDao;
import models.entities.AbstractUser;
import services.AbstractUserService;
import services.AnonUserService;
import services.UserService;

/**
 * Created by kdoherty on 7/1/15.
 */
public class AbstractUserServiceImpl extends GenericServiceImpl<AbstractUser> implements AbstractUserService {

    @Inject
    public AbstractUserServiceImpl(AbstractUserDao abstractUserDao, UserService userService, AnonUserService anonUserService) {
        super(abstractUserDao);
    }
}
