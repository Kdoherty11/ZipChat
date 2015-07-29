package daos;

import com.google.inject.ImplementedBy;
import models.entities.Device;
import models.entities.User;
import daos.impl.UserDaoImpl;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(UserDaoImpl.class)
public interface UserDao extends GenericDao<User> {
    Optional<User> findByFacebookId(String facebookId);
    List<Device> getDevices(User user);
    void merge(User user);

}
