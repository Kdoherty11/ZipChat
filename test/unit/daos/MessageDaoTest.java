package unit.daos;

import daos.MessageDao;
import daos.PublicRoomDao;
import daos.UserDao;
import daos.impl.MessageDaoImpl;
import daos.impl.PublicRoomDaoImpl;
import daos.impl.UserDaoImpl;
import factories.MessageFactory;
import factories.ObjectMutator;
import factories.PublicRoomFactory;
import factories.UserFactory;
import models.entities.Message;
import models.entities.PublicRoom;
import models.entities.User;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import play.test.WithApplication;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by kdoherty on 7/30/15.
 */
public class MessageDaoTest extends WithApplication {

    private MessageDao messageDao;
    private MessageFactory messageFactory;
    private PublicRoomFactory publicRoomFactory;
    private PublicRoomDao publicRoomDao;
    private UserFactory userFactory;
    private UserDao userDao;

    @Before
    public void setUp() {
        messageDao = new MessageDaoImpl();
        messageFactory = new MessageFactory();
        publicRoomFactory = new PublicRoomFactory();
        publicRoomDao = new PublicRoomDaoImpl();
        userFactory = new UserFactory();
        userDao = new UserDaoImpl();
    }

    @Test
    public void getMessagesReturnsAnEmptyListIfNoMessagesExist() {
        JPA.withTransaction(() -> {
            List<Message> messages = messageDao.getMessages(1, 1, 1);
            assertTrue(messages.isEmpty());
        });
    }

    @Test
    public void getMessagesLimitsTheMessagesReturned() {
        JPA.withTransaction(() -> {
            PublicRoom room = publicRoomFactory.create();
            User sender = userFactory.create();

            publicRoomDao.save(room);
            userDao.save(sender);
            JPA.em().flush();

            List<Message> roomMessages = messageFactory.createList(3, new ObjectMutator<Message>() {
                @Override
                public void apply(Message message) throws IllegalAccessException, InstantiationException {
                    message.room = room;
                    message.sender = sender;
                }
            });

            roomMessages.forEach(messageDao::save);

            int limit = 2;
            List<Message> messages = messageDao.getMessages(room.roomId, limit, 0);
            assertEquals(limit, messages.size());
        });
    }

    @Test
    public void getMessagesReturnsTheMessagesInReverseOrderByTimeStamp() {
        JPA.withTransaction(() -> {
            PublicRoom room = publicRoomFactory.create();
            User sender = userFactory.create();

            publicRoomDao.save(room);
            userDao.save(sender);
            JPA.em().flush();

            final AtomicLong createdAt = new AtomicLong(1);
            List<Message> roomMessages = messageFactory.createList(3, new ObjectMutator<Message>() {
                @Override
                public void apply(Message message) throws IllegalAccessException, InstantiationException {
                    message.room = room;
                    message.sender = sender;
                    message.createdAt = createdAt.getAndIncrement();
                }
            });

            roomMessages.forEach(messageDao::save);

            // Most recent messages should be at the start of the list
            List<Message> messages = messageDao.getMessages(room.roomId, 3, 0);
            assertEquals(1l, messages.get(0).createdAt);
            assertEquals(2l, messages.get(1).createdAt);
            assertEquals(3l, messages.get(2).createdAt);
        });
    }

    @Test
    public void getMessagesRespectsOffset() {
        JPA.withTransaction(() -> {
            PublicRoom room = publicRoomFactory.create();
            User sender = userFactory.create();

            publicRoomDao.save(room);
            userDao.save(sender);
            JPA.em().flush();

            final AtomicLong createdAt = new AtomicLong(1);
            List<Message> roomMessages = messageFactory.createList(3, new ObjectMutator<Message>() {
                @Override
                public void apply(Message message) throws IllegalAccessException, InstantiationException {
                    message.room = room;
                    message.sender = sender;
                    message.createdAt = createdAt.getAndIncrement();
                }
            });

            roomMessages.forEach(messageDao::save);

            // Most recent messages should be at the start of the list
            int offset = 1;
            int limit = 3;
            List<Message> messages = messageDao.getMessages(room.roomId, limit, offset);
            assertEquals(Math.min(roomMessages.size() - offset, limit), messages.size());
            // Offset starts from end of list (most recent messages)
            assertEquals(roomMessages.get(0), messages.get(0));
            assertEquals(roomMessages.get(1), messages.get(1));
        });
    }

    @Test
    public void getMessagesDoesNotIncludeMessagesNotInRoom() {
        JPA.withTransaction(() -> {
            PublicRoom room = publicRoomFactory.create();
            PublicRoom other = publicRoomFactory.create();
            User sender = userFactory.create();

            publicRoomDao.save(room);
            publicRoomDao.save(other);
            userDao.save(sender);
            JPA.em().flush();

            int numRoomMessages = 2;
            List<Message> roomMessages = messageFactory.createList(numRoomMessages, new ObjectMutator<Message>() {
                @Override
                public void apply(Message message) throws IllegalAccessException, InstantiationException {
                    message.room = room;
                    message.sender = sender;
                }
            });
            roomMessages.forEach(messageDao::save);

            Message notInRoomMsg = messageFactory.create(new ObjectMutator<Message>() {
                @Override
                public void apply(Message message) throws IllegalAccessException, InstantiationException {
                    message.room = other;
                    message.sender = sender;
                }
            });
            messageDao.save(notInRoomMsg);

            List<Message> messages = messageDao.getMessages(room.roomId, 5, 0);
            assertEquals(numRoomMessages, messages.size());
            assertFalse((messages.contains(notInRoomMsg)));
        });
    }
}
