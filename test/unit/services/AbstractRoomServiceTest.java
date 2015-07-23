package unit.services;

import daos.AbstractRoomDao;
import factories.MessageFactory;
import factories.PrivateRoomFactory;
import factories.PropOverride;
import factories.PublicRoomFactory;
import models.entities.Message;
import models.entities.PrivateRoom;
import models.entities.PublicRoom;
import notifications.MessageNotification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.AbstractRoomService;
import services.PublicRoomService;
import services.UserService;
import services.impl.AbstractRoomServiceImpl;
import utils.TestUtils;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by kdoherty on 7/7/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractRoomServiceTest {

    private AbstractRoomService abstractRoomService;
    private PublicRoomFactory publicRoomFactory;
    private PrivateRoomFactory privateRoomFactory;
    private MessageFactory messageFactory;

    @Mock
    private AbstractRoomDao abstractRoomDao;

    @Mock
    private PublicRoomService publicRoomService;

    @Mock
    private UserService userService;

    @Before
    public void setUp() {
        abstractRoomService = new AbstractRoomServiceImpl(abstractRoomDao, publicRoomService, userService);
        publicRoomFactory = new PublicRoomFactory();
        privateRoomFactory = new PrivateRoomFactory();
        messageFactory = new MessageFactory();
    }

    @Test
    public void addMessageToPublicRoom() throws InstantiationException, IllegalAccessException {
        PublicRoom room = publicRoomFactory.create(PropOverride.of("lastActivity", -1));
        Message mockMessage = messageFactory.create(PropOverride.of("room", room),
                MessageFactory.FactoryTrait.WITH_SENDER);
        Set<Long> userIdsInRoom = Collections.emptySet();

        abstractRoomService.addMessage(room, mockMessage, userIdsInRoom);

        assertEquals(1, room.messages.size());
        assertTrue(room.messages.contains(mockMessage));
        assertTrue(room.lastActivity > 0);
        verify(publicRoomService).sendNotification(
                refEq(room), any(MessageNotification.class), refEq(userIdsInRoom));
    }

    @Test
    public void addMessageToPublicRoomAddsToRoomMessages() throws InstantiationException, IllegalAccessException {
        PublicRoom room = publicRoomFactory.create();
        Message message = messageFactory.create(PropOverride.of("room", room), MessageFactory.FactoryTrait.WITH_SENDER);

        abstractRoomService.addMessage(room, message, Collections.emptySet());

        assertEquals(1, room.messages.size());
        assertTrue(room.messages.contains(message));
    }

    @Test
    public void addMessageToPublicRoomSetsRoomLastActivity() throws InstantiationException, IllegalAccessException {
        PublicRoom room = publicRoomFactory.create(PropOverride.of("lastActivity", -1));
        Message message = messageFactory.create(PropOverride.of("room", room), MessageFactory.FactoryTrait.WITH_SENDER);

        abstractRoomService.addMessage(room, message, Collections.emptySet());

        assertTrue(room.lastActivity > 0);
    }

    @Test
    public void addMessageToPrivateRoomAddsToRoomMessages() throws InstantiationException, IllegalAccessException {
        PrivateRoom room = privateRoomFactory.create(PrivateRoomFactory.Trait.WITH_SENDER_AND_RECEIVER);
        Message message = messageFactory.create(PropOverride.of("room", room), PropOverride.of("sender", room.sender));

        abstractRoomService.addMessage(room, message, Collections.emptySet());

        assertEquals(1, room.messages.size());
        assertTrue(room.messages.contains(message));
    }

    @Test
    public void addMessageToPrivateRoomSetsRoomLastActivity() throws InstantiationException, IllegalAccessException {
        PrivateRoom room = privateRoomFactory.create(PrivateRoomFactory.Trait.WITH_SENDER_AND_RECEIVER, PropOverride.of("lastActivity", -1));
        Message message = messageFactory.create(PropOverride.of("room", room), PropOverride.of("sender", room.sender));

        abstractRoomService.addMessage(room, message, Collections.emptySet());

        assertTrue(room.lastActivity > 0);
    }

    @Test
    public void addMessageToPrivateRoomSendsNotificationToCorrectUser() throws InstantiationException, IllegalAccessException {
        PrivateRoom room = privateRoomFactory.create(PrivateRoomFactory.Trait.WITH_SENDER_AND_RECEIVER, PropOverride.of("lastActivity", -1));

        Message message = messageFactory.create(PropOverride.of("room", room), PropOverride.of("sender", room.sender));
        abstractRoomService.addMessage(room, message, Collections.emptySet());
        verify(userService).sendNotification(refEq(room.receiver), any(MessageNotification.class));

        Message response = messageFactory.create(PropOverride.of("room", room), PropOverride.of("sender", room.receiver));
        abstractRoomService.addMessage(room, response, Collections.emptySet());
        verify(userService).sendNotification(refEq(room.sender), any(MessageNotification.class));
    }

    @Test
    public void addMessageToPrivateRoomDoesNotSendNotificationIfReceiverIsInRoom() throws InstantiationException, IllegalAccessException {
        PrivateRoom room = privateRoomFactory.create(PrivateRoomFactory.Trait.WITH_SENDER_AND_RECEIVER, PropOverride.of("lastActivity", -1));
        long senderId = 1;
        long receiverId = 2;
        room.sender.userId = senderId;
        room.receiver.userId = receiverId;

        Message message = messageFactory.create(PropOverride.of("room", room), PropOverride.of("sender", room.sender));
        abstractRoomService.addMessage(room, message, TestUtils.setOf(receiverId));
        verifyZeroInteractions(userService);
    }




}
