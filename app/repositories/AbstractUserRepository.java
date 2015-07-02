package repositories;

import com.google.inject.ImplementedBy;
import models.entities.AbstractUser;
import repositories.impl.AbstractUserRepositoryImpl;

import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(AbstractUserRepositoryImpl.class)
public interface AbstractUserRepository {

    public Optional<AbstractUser> findById(long userId);
}
