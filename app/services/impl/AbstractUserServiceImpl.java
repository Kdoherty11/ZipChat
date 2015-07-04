package services.impl;

import com.google.inject.Inject;
import models.entities.AbstractUser;
import repositories.AbstractUserRepository;
import services.AbstractUserService;

/**
 * Created by kdoherty on 7/1/15.
 */
public class AbstractUserServiceImpl extends GenericServiceImpl<AbstractUser> implements AbstractUserService {

    @Inject
    public AbstractUserServiceImpl(AbstractUserRepository abstractUserRepository) {
        super(abstractUserRepository);
    }
}
