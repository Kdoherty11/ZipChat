package unit.daos;

import daos.PublicRoomDao;
import daos.impl.PublicRoomDaoImpl;
import factories.PropOverride;
import factories.PublicRoomFactory;
import models.entities.PublicRoom;
import models.entities.User;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import play.test.WithApplication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by kdoherty on 7/30/15.
 */
public class PublicRoomDaoTest extends WithApplication {

    private PublicRoomDao publicRoomDao;
    private PublicRoomFactory publicRoomFactory;

    @Before
    public void setUp() {
        publicRoomDao = new PublicRoomDaoImpl();
        publicRoomFactory = new PublicRoomFactory();
    }

    @Test
    public void allInGeoRangeReturnsEmptyIfNoRoomsExist() {
        JPA.withTransaction(() -> {
            List<PublicRoom> rooms = publicRoomDao.allInGeoRange(100, 200);
            assertTrue(rooms.isEmpty());
        });
    }

    @Test
    public void allInGeoRangeOnlyReturnsRoomsWithinTheRoomDistanceAway() {
        double lat1 = 50.00;
        double lon1 = 50.00;

        double lat2 = 50.01;
        double lon2 = 50.01;

        // distance between lat1, lon1 and lat2, lon2
        int meterDistance = 1322;

        JPA.withTransaction(() -> {
            PropOverride latOverride = PropOverride.of("latitude", lat1);
            PropOverride lonOverride = PropOverride.of("longitude", lon1);

            PublicRoom room1 = publicRoomFactory.create(latOverride, lonOverride, PropOverride.of("radius", meterDistance), PublicRoomFactory.Trait.PERSISTED);
            PublicRoom room2 = publicRoomFactory.create(latOverride, lonOverride, PropOverride.of("radius", meterDistance - 1), PublicRoomFactory.Trait.PERSISTED);
            PublicRoom room3 = publicRoomFactory.create(latOverride, lonOverride, PropOverride.of("radius", meterDistance + 1), PublicRoomFactory.Trait.PERSISTED);

            List<PublicRoom> rooms = publicRoomDao.allInGeoRange(lat2, lon2);
            assertEquals(2, rooms.size());
            assertTrue(rooms.contains(room1));
            assertFalse(rooms.contains(room2));
            assertTrue(rooms.contains(room3));
        });
    }

    @Test
    public void getSubscribersReturnsRoomsSubscribers() {
        Set<User> users = new HashSet<>();
        PublicRoom room = mock(PublicRoom.class);
        when(room.subscribers).thenReturn(users);
        assertSame(users, publicRoomDao.getSubscribers(room));
    }



}
