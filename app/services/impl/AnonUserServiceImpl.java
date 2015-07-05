package services.impl;

import com.google.inject.Inject;
import daos.AnonUserDao;
import models.entities.AnonUser;
import notifications.AbstractNotification;
import services.AnonUserService;
import services.UserService;

/**
 * Created by kdoherty on 7/4/15.
 */
public class AnonUserServiceImpl extends GenericServiceImpl<AnonUser> implements AnonUserService {

    private UserService userService;

    @Inject
    public AnonUserServiceImpl(final AnonUserDao anonUserDao, final UserService userService) {
        super(anonUserDao);
        this.userService = userService;
    }

    @Override
    public void sendNotification(AnonUser receiver, AbstractNotification notification) {
        userService.sendNotification(receiver.actual, notification);
    }
}
