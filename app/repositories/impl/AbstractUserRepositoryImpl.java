package repositories.impl;

import models.entities.AbstractUser;
import repositories.AbstractUserRepository;

/**
 * Created by kdoherty on 6/30/15.
 */
public class AbstractUserRepositoryImpl extends GenericRepositoryImpl<AbstractUser> implements AbstractUserRepository {

    public AbstractUserRepositoryImpl() {
        super(AbstractUser.class);
    }
}
