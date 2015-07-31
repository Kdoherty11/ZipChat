package unit.daos;

import daos.GenericDao;
import daos.impl.GenericDaoImpl;
import factories.UserFactory;
import models.entities.User;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by kdoherty on 7/30/15.
 */
public class GenericDaoTest extends AbstractDaoTest {

    private GenericDao<User> genericDao;
    private UserFactory userFactory;

    @Before
    public void setUp() {
        genericDao = new GenericDaoImpl<>(User.class);
        this.userFactory = new UserFactory();
    }

    @Test(expected = NullPointerException.class)
    public void constructorArgCantBeNull() {
        genericDao = new GenericDaoImpl<>(null);
    }

    @Test
    public void persistSetsId() {
        JPA.withTransaction(() -> {
            User user = userFactory.create();
            genericDao.save(user);
            JPA.em().flush();
            assertTrue(user.userId > 0);
        });
    }

    @Test
    public void persistedEntitiesCanBeFound() throws Throwable {
        User user = userFactory.create();

        JPA.withTransaction(() -> {
            genericDao.save(user);
            JPA.em().flush();

            Optional<User> retrievedUserOptional = genericDao.findById(user.userId);
            assertTrue(retrievedUserOptional.isPresent());
            return user.userId;
        });
    }

    @Test
    public void persistedEntitiesCanBeRemoved() throws Throwable {
        User user = userFactory.create();

        JPA.withTransaction(() -> {
            genericDao.save(user);
            JPA.em().flush();
            long userId = user.userId;

            genericDao.remove(user);

            Optional<User> retrievedUserOptional = genericDao.findById(userId);
            assertFalse(retrievedUserOptional.isPresent());
        });
    }

    @Test
    public void entitiesCanBeMerged() throws InstantiationException, IllegalAccessException {
        User existing = userFactory.create();

        JPA.withTransaction(() -> {
            genericDao.save(existing);
            JPA.em().flush();

            String newName = UUID.randomUUID().toString();
            User newUser = new User();
            newUser.userId = existing.userId;
            newUser.gender = existing.gender;
            newUser.name = newName;
            genericDao.merge(newUser);

            assertEquals(newName, newUser.name);
            assertEquals(existing.facebookId, newUser.facebookId);
            assertEquals(existing.gender, newUser.gender);
        });
    }




}
