package repositories;

import com.google.inject.ImplementedBy;
import models.entities.Device;
import models.entities.User;
import repositories.impl.UserRepositoryImpl;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(UserRepositoryImpl.class)
public interface UserRepository extends GenericRepository<User> {
    Optional<User> findByFacebookId(String facebookId);
    List<Device> getDevices(User user);

}
