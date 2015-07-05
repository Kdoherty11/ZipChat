package services.impl;

import com.google.inject.Inject;
import daos.AbstractUserDao;
import models.entities.AbstractUser;
import models.entities.AnonUser;
import models.entities.User;
import notifications.AbstractNotification;
import services.AbstractUserService;
import services.AnonUserService;
import services.UserService;

/**
 * Created by kdoherty on 7/1/15.
 */
public class AbstractUserServiceImpl extends GenericServiceImpl<AbstractUser> implements AbstractUserService {

    private final UserService userService;
    private final AnonUserService anonUserService;

    @Inject
    public AbstractUserServiceImpl(AbstractUserDao abstractUserDao, UserService userService, AnonUserService anonUserService) {
        super(abstractUserDao);
        this.userService = userService;
        this.anonUserService = anonUserService;
    }

    @Override
    public void sendNotification(AbstractUser abstractUser, AbstractNotification notification) {
        if (abstractUser instanceof User) {
            userService.sendNotification((User) abstractUser, notification);
        } else {
            anonUserService.sendNotification((AnonUser) abstractUser, notification);
        }
    }
}
