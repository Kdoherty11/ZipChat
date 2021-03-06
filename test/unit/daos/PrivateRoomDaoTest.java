package unit.daos;

import daos.PrivateRoomDao;
import daos.impl.PrivateRoomDaoImpl;
import factories.PrivateRoomFactory;
import factories.FieldOverride;
import models.PrivateRoom;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by kdoherty on 7/30/15.
 */
public class PrivateRoomDaoTest extends AbstractDaoTest {

    private PrivateRoomDao privateRoomDao;
    private PrivateRoomFactory privateRoomFactory;

    @Before
    public void setUp() {
        privateRoomDao = new PrivateRoomDaoImpl();
        privateRoomFactory = new PrivateRoomFactory();
    }

    @Test
    public void findByUserIdReturnsEmptyWhenNotSenderOrReceiver() {
        JPA.withTransaction(() -> {
            List<PrivateRoom> privateRooms = privateRoomDao.findByUserId(1);
            assertTrue(privateRooms.isEmpty());
        });
    }

    @Test
    public void findByUserIdReturnsEmptyWhenSenderAndNotInRoom() {
        JPA.withTransaction(() -> {
            PrivateRoom room = privateRoomFactory.create(FieldOverride.of("senderInRoom", false), PrivateRoomFactory.Trait.PERSISTED_WITH_REQUEST);
            List<PrivateRoom> privateRooms = privateRoomDao.findByUserId(room.sender.userId);
            assertTrue(privateRooms.isEmpty());
        });
    }

    @Test
    public void findByUserIdReturnsEmptyWhenReceiverAndNotInRoom() {
        JPA.withTransaction(() -> {
            PrivateRoom room = privateRoomFactory.create(FieldOverride.of("receiverInRoom", false), PrivateRoomFactory.Trait.PERSISTED_WITH_REQUEST);
            List<PrivateRoom> privateRooms = privateRoomDao.findByUserId(room.receiver.userId);
            assertTrue(privateRooms.isEmpty());
        });
    }

    @Test
    public void findByUserIdReturnsNonEmptyWhenUserIsSenderAndIsInRoom() {
        JPA.withTransaction(() -> {
            PrivateRoom room = privateRoomFactory.create(PrivateRoomFactory.Trait.PERSISTED_WITH_REQUEST);
            List<PrivateRoom> privateRooms = privateRoomDao.findByUserId(room.sender.userId);
            assertFalse(privateRooms.isEmpty());
            assertTrue(privateRooms.contains(room));
        });
    }

    @Test
    public void findByUserIdReturnsNonEmptyWhenUserIsReceiverAndIsInRoom() {
        JPA.withTransaction(() -> {
            PrivateRoom room = privateRoomFactory.create(PrivateRoomFactory.Trait.PERSISTED_WITH_REQUEST);
            List<PrivateRoom> privateRooms = privateRoomDao.findByUserId(room.receiver.userId);
            assertFalse(privateRooms.isEmpty());
            assertTrue(privateRooms.contains(room));
        });
    }

    @Test
    public void findByActiveRoomMembersReturnEmptyIfNoneExists() {
        JPA.withTransaction(() -> {
            Optional<PrivateRoom> optional = privateRoomDao.findByActiveRoomMembers(1, 2);
            assertFalse(optional.isPresent());
        });
    }

    @Test
    public void findByActiveRoomMembersReturnsEmptyIfSenderNotInRoom() {
        JPA.withTransaction(() -> {
            PrivateRoom room = privateRoomFactory.create(
                    PrivateRoomFactory.Trait.WITH_PERSISTED_REQUEST,
                    FieldOverride.of("senderInRoom", false),
                    PrivateRoomFactory.Trait.PERSISTED);
            Optional<PrivateRoom> optional = privateRoomDao.findByActiveRoomMembers(room.sender.userId, room.receiver.userId);
            assertFalse(optional.isPresent());
        });
    }

    @Test
    public void findByActiveRoomMembersReturnsEmptyIfReceiverNotInRoom() {
        JPA.withTransaction(() -> {
            PrivateRoom room = privateRoomFactory.create(
                    PrivateRoomFactory.Trait.WITH_PERSISTED_REQUEST,
                    FieldOverride.of("receiverInRoom", false),
                    PrivateRoomFactory.Trait.PERSISTED);
            Optional<PrivateRoom> optional = privateRoomDao.findByActiveRoomMembers(room.sender.userId, room.receiver.userId);
            assertFalse(optional.isPresent());
        });
    }

    @Test
    public void findByActiveRoomMembersReturnsNonEmptyIfBothMatchAndBothAreInRoom() {
        JPA.withTransaction(() -> {
            PrivateRoom room = privateRoomFactory.create(PrivateRoomFactory.Trait.PERSISTED_WITH_REQUEST);
            JPA.em().flush();
            Optional<PrivateRoom> optional = privateRoomDao.findByActiveRoomMembers(room.sender.userId, room.receiver.userId);
            assertTrue(optional.isPresent());

            Optional<PrivateRoom> switchedOptional = privateRoomDao.findByActiveRoomMembers(room.receiver.userId, room.sender.userId);
            assertTrue(switchedOptional.isPresent());
        });
    }

    @Test
    public void findByRoomMembersReturnsRegardlessOfIfUsersHaveLeftTheRoom() {
        JPA.withTransaction(() -> {
            PrivateRoom room = privateRoomFactory.create(
                    PrivateRoomFactory.Trait.WITH_PERSISTED_REQUEST,
                    FieldOverride.of("receiverInRoom", false),
                    PrivateRoomFactory.Trait.PERSISTED);

            Optional<PrivateRoom> optional = privateRoomDao.findByRoomMembers(room.sender.userId, room.receiver.userId);
            assertTrue(optional.isPresent());

            Optional<PrivateRoom> switchedOptional = privateRoomDao.findByRoomMembers(room.receiver.userId, room.sender.userId);
            assertTrue(switchedOptional.isPresent());
        });
    }

    @Test
    public void findByRoomMembersReturnEmptyIfNoneExists() {
        JPA.withTransaction(() -> {
            Optional<PrivateRoom> optional = privateRoomDao.findByRoomMembers(1, 2);
            assertFalse(optional.isPresent());
        });
    }


}
