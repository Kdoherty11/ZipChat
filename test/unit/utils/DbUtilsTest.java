package unit.utils;

import daos.UserDao;
import daos.impl.UserDaoImpl;
import factories.UserFactory;
import models.User;
import org.junit.Test;
import play.db.jpa.JPA;
import play.test.WithApplication;
import utils.DbUtils;
import utils.TestUtils;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;

/**
 * Created by kdoherty on 8/2/15.
 */
public class DbUtilsTest extends WithApplication {

    @Test
    public void testPrivateConstructor() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TestUtils.testConstructorIsPrivate(DbUtils.class);
    }

    @Test(expected = RuntimeException.class)
    public void findExistingEntityByIdNoneFound() {
        JPA.withTransaction(() -> {
            DbUtils.findExistingEntityById(User.class, 1);
        });
    }

    @Test
    public void findExistingEntityFound() throws InstantiationException, IllegalAccessException {
        UserDao userDao = new UserDaoImpl();
        User user = new UserFactory().create();

        JPA.withTransaction(() -> {
            userDao.save(user);
            JPA.em().flush();
            User foundUser = DbUtils.findExistingEntityById(User.class, user.userId);
            assertEquals(user, foundUser);
        });
    }

}
