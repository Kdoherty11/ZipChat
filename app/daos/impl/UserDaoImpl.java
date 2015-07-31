package daos.impl;

import daos.UserDao;
import models.entities.Device;
import models.entities.User;
import play.db.jpa.JPA;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
public class UserDaoImpl extends GenericDaoImpl<User> implements UserDao {

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    public Optional<User> findByFacebookId(String facebookId) {
        String queryString = "select u from User u where u.facebookId = :facebookId";

        TypedQuery<User> query = JPA.em()
                .createQuery(queryString, User.class)
                .setParameter("facebookId", facebookId);

        List<User> users = query.getResultList();
        if (users.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(users.get(0));
        }
    }

    @Override
    public List<Device> getDevices(User user) {
        return user.devices;
    }
}
