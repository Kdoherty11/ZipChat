package unit.daos;

import daos.UserDao;
import daos.impl.UserDaoImpl;
import factories.DeviceFactory;
import factories.ObjectMutator;
import factories.UserFactory;
import models.Device;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by kdoherty on 7/30/15.
 */
public class UserDaoTest extends AbstractDaoTest {

    private UserDao userDao;
    private UserFactory userFactory;

    @Before
    public void setUp() {
        userDao = new UserDaoImpl();
        userFactory = new UserFactory();
    }

    @Test
    public void findByFacebookIdReturnsEmptyIfNoUserExists() {
        JPA.withTransaction(() -> {
            Optional<User> userOptional = userDao.findByFacebookId("facebookId");
            assertFalse(userOptional.isPresent());
        });
    }

    @Test
    public void findByFacebookIdReturnsNonEmptyIfUserExists() throws InstantiationException, IllegalAccessException {
        User user = userFactory.create();

        JPA.withTransaction(() -> {
            userDao.save(user);
            Optional<User> userOptional = userDao.findByFacebookId(user.facebookId);
            assertTrue(userOptional.isPresent());
        });
    }

    @Test
    public void getDevices() throws InstantiationException, IllegalAccessException {
        User user = userFactory.create();

        JPA.withTransaction(() -> {
            List<Device> expectedDevices = new DeviceFactory().createList(2, new ObjectMutator<Device>() {
                @Override
                public void apply(Device device) throws IllegalAccessException, InstantiationException {
                    device.user = user;
                }
            });
            user.devices = expectedDevices;
            userDao.save(user);

            List<Device> actualDevices = userDao.getDevices(user);

            assertEquals(expectedDevices.size(), actualDevices.size());
            assertTrue(expectedDevices.containsAll(actualDevices));
            assertTrue(actualDevices.containsAll(expectedDevices));
        });
    }

}
