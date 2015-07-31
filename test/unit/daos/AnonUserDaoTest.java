package unit.daos;

import daos.AnonUserDao;
import daos.impl.AnonUserDaoImpl;
import factories.AnonUserFactory;
import factories.PublicRoomFactory;
import factories.UserFactory;
import models.entities.AnonUser;
import models.entities.PublicRoom;
import models.entities.User;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import utils.TestUtils;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by kdoherty on 7/30/15.
 */
public class AnonUserDaoTest extends AbstractDaoTest {

    private AnonUserDao anonUserDao;
    private AnonUserFactory anonUserFactory;
    private UserFactory userFactory;
    private PublicRoomFactory publicRoomFactory;

    @Before
    public void setUp() {
        this.anonUserDao = new AnonUserDaoImpl();
        this.anonUserFactory = new AnonUserFactory();
        this.userFactory = new UserFactory();
        this.publicRoomFactory = new PublicRoomFactory();
    }

    @Test
    public void getAnonUserReturnsEmptyOneDoesNotExist() throws InstantiationException, IllegalAccessException {
        JPA.withTransaction(() -> {
            Optional<AnonUser> anonUserOptional = anonUserDao.getAnonUser(userFactory.create(), publicRoomFactory.create());
            assertFalse(anonUserOptional.isPresent());
        });
    }

    @Test
    public void getAnonUserReturnsEmptyIfOnlyRoomMatches() {
        JPA.withTransaction(() -> {
            AnonUser anonUser = anonUserFactory.create(AnonUserFactory.Trait.PERSISTED_WITH_ACTUAL_AND_ROOM);
            User other = userFactory.create();
            other.userId = TestUtils.getUniqueId(anonUser, anonUser.actual);
            Optional<AnonUser> anonUserOptional = anonUserDao.getAnonUser(other, anonUser.room);
            assertFalse(anonUserOptional.isPresent());
        });
    }

    @Test
    public void getAnonUserReturnsEmptyIfOnlyActualMatches() {
        JPA.withTransaction(() -> {
            AnonUser anonUser = anonUserFactory.create(AnonUserFactory.Trait.PERSISTED_WITH_ACTUAL_AND_ROOM);
            PublicRoom other = publicRoomFactory.create();
            other.roomId = TestUtils.getUniqueId(anonUser.room);

            Optional<AnonUser> anonUserOptional = anonUserDao.getAnonUser(anonUser.actual, other);
            assertFalse(anonUserOptional.isPresent());
        });
    }

    @Test
    public void getAnonUserReturnsAnonUserIfRoomAndActualMatch() {
        JPA.withTransaction(() -> {
            AnonUser anonUser = anonUserFactory.create(AnonUserFactory.Trait.PERSISTED_WITH_ACTUAL_AND_ROOM);
            Optional<AnonUser> anonUserOptional = anonUserDao.getAnonUser(anonUser.actual, anonUser.room);
            assertTrue(anonUserOptional.isPresent());
            assertEquals(anonUser, anonUserOptional.get());
        });
    }

}
