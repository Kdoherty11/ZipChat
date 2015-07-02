package repositories;

import com.google.inject.ImplementedBy;
import models.entities.User;
import repositories.impl.UserRepositoryImpl;

import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(UserRepositoryImpl.class)
public interface UserRepository {

    Optional<User> findById(long userId);
    Optional<User> findByFacebookId(String facebookId);
}
