package repositories;

import com.google.inject.ImplementedBy;
import models.entities.AbstractUser;
import repositories.impl.AbstractUserRepositoryImpl;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(AbstractUserRepositoryImpl.class)
public interface AbstractUserRepository extends GenericRepository<AbstractUser> {
}
