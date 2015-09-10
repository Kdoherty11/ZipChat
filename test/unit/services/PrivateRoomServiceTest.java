package unit.services;

import daos.PrivateRoomDao;
import daos.RequestDao;
import models.PrivateRoom;
import models.Request;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.PrivateRoomService;
import services.impl.PrivateRoomServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 7/6/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class PrivateRoomServiceTest {

    private PrivateRoomService privateRoomService;

    @Mock
    private PrivateRoomDao privateRoomDao;

    @Mock
    private RequestDao requestDao;

    @Before
    public void setUp() {
        privateRoomService = spy(new PrivateRoomServiceImpl(privateRoomDao, requestDao));
    }

    @Test
    public void removeUserNotInRoom() throws InstantiationException, IllegalAccessException {
        User mockSender = mock(User.class);
        User mockReceiver = mock(User.class);
        when(mockSender.userId).thenReturn(1l);
        when(mockReceiver.userId).thenReturn(2l);

        Request request = new Request(mockSender, mockReceiver);
        PrivateRoom privateRoom = new PrivateRoom(request);

        boolean removed = privateRoomService.removeUser(privateRoom, 3l);

        assertFalse(removed);
        assertTrue(privateRoom.senderInRoom);
        assertTrue(privateRoom.receiverInRoom);
        verifyZeroInteractions(requestDao, privateRoomDao);
        assertNotNull(privateRoom.request);
    }

    @Test
    public void removeSender() {
        User mockSender = mock(User.class);
        User mockReceiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(mockSender.userId).thenReturn(senderId);
        when(mockReceiver.userId).thenReturn(receiverId);

        Request request = new Request(mockSender, mockReceiver);
        PrivateRoom privateRoom = new PrivateRoom(request);

        boolean removed = privateRoomService.removeUser(privateRoom, senderId);

        assertTrue(removed);
        assertFalse(privateRoom.senderInRoom);
        assertTrue(privateRoom.receiverInRoom);
        verifyZeroInteractions(privateRoomDao);
        verify(requestDao).remove(request);
        assertNull(privateRoom.request);
    }

    @Test
    public void removeReceiver() {
        User mockSender = mock(User.class);
        User mockReceiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(mockSender.userId).thenReturn(senderId);
        when(mockReceiver.userId).thenReturn(receiverId);

        Request request = new Request(mockSender, mockReceiver);
        PrivateRoom privateRoom = new PrivateRoom(request);

        boolean removed = privateRoomService.removeUser(privateRoom, receiverId);

        assertTrue(removed);
        assertTrue(privateRoom.senderInRoom);
        assertFalse(privateRoom.receiverInRoom);
        verifyZeroInteractions(privateRoomDao);
        verify(requestDao).remove(request);
        assertNull(privateRoom.request);
    }

    @Test
    public void isUserInRoomSenderTrue() {
        User sender = mock(User.class);
        User receiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(sender.userId).thenReturn(senderId);
        when(receiver.userId).thenReturn(receiverId);
        Request request = new Request(sender, receiver);
        PrivateRoom room = new PrivateRoom(request);

        boolean isUserInRoom = privateRoomService.isUserInRoom(room, senderId);
        assertTrue(isUserInRoom);
    }

    @Test
    public void isUserInRoomReceiverTrue() {
        User sender = mock(User.class);
        User receiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(sender.userId).thenReturn(senderId);
        when(receiver.userId).thenReturn(receiverId);
        Request request = new Request(sender, receiver);
        PrivateRoom room = new PrivateRoom(request);

        boolean isUserInRoom = privateRoomService.isUserInRoom(room, receiverId);
        assertTrue(isUserInRoom);
    }

    @Test
    public void isUserInRoomReceiverFalse() {
        User sender = mock(User.class);
        User receiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(sender.userId).thenReturn(senderId);
        when(receiver.userId).thenReturn(receiverId);
        Request request = new Request(sender, receiver);
        PrivateRoom room = new PrivateRoom(request);
        room.receiverInRoom = false;

        boolean isUserInRoom = privateRoomService.isUserInRoom(room, receiverId);
        assertFalse(isUserInRoom);
    }

    @Test
    public void isUserInRoomSenderFalse() {
        User sender = mock(User.class);
        User receiver = mock(User.class);
        long senderId = 1;
        long receiverId = 2;
        when(sender.userId).thenReturn(senderId);
        when(receiver.userId).thenReturn(receiverId);
        Request request = new Request(sender, receiver);
        PrivateRoom room = new PrivateRoom(request);
        room.senderInRoom = false;

        boolean isUserInRoom = privateRoomService.isUserInRoom(room, senderId);
        assertFalse(isUserInRoom);
    }

    @Test
    public void findByUserId() {
        long userId = 1;
        List<PrivateRoom> expected = new ArrayList<>();
        when(privateRoomDao.findByUserId(userId)).thenReturn(expected);
        List<PrivateRoom> actual = privateRoomService.findByUserId(userId);
        assertSame(expected, actual);
    }

    @Test
    public void findBySenderAndReceiver() {
        long senderId = 1;
        long receiverId = 2;
        Optional<PrivateRoom> expected = Optional.empty();

        when(privateRoomDao.findByActiveRoomMembers(senderId, receiverId)).thenReturn(expected);

        Optional<PrivateRoom> actual = privateRoomService.findByActiveRoomMembers(senderId, receiverId);

        assertSame(expected, actual);
    }
}
