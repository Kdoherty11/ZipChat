package repositories.impl;

import models.entities.AbstractUser;
import repositories.AbstractUserRepository;
import utils.DbUtils;

import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
public class AbstractUserRepositoryImpl implements AbstractUserRepository {

    @Override
    public Optional<AbstractUser> findById(long userId) {
        return DbUtils.findEntityById(AbstractUser.class, userId);
    }
}
