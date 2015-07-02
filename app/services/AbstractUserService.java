package services;

import com.google.inject.Inject;
import models.entities.AbstractUser;
import repositories.AbstractUserRepository;

import java.util.Optional;

/**
 * Created by kdoherty on 7/1/15.
 */
public class AbstractUserService implements AbstractUserRepository {

    private AbstractUserRepository abstractUserRepository;

    @Inject
    public AbstractUserService(AbstractUserRepository abstractUserRepository) {
        this.abstractUserRepository = abstractUserRepository;
    }

    @Override
    public Optional<AbstractUser> findById(long userId) {
        return abstractUserRepository.findById(userId);
    }
}
