package repositories.impl;

import models.entities.User;
import play.db.jpa.JPA;
import repositories.UserRepository;
import utils.DbUtils;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
public class UserRepositoryImpl implements UserRepository {

    @Override
    public Optional<User> findById(long userId) {
        return DbUtils.findEntityById(User.class, userId);
    }

    @Override
    public Optional<User> findByFacebookId(String facebookId) {
        String queryString = "select u from User u where u.facebookId = :facebookId";

        TypedQuery<User> query = JPA.em().createQuery(queryString, User.class)
                .setParameter("facebookId", facebookId);

        List<User> users = query.getResultList();
        if (users.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(users.get(0));
        }
    }
}
