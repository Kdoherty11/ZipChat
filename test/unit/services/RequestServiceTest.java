package unit.services;

import daos.PrivateRoomDao;
import daos.RequestDao;
import factories.FieldOverride;
import factories.PrivateRoomFactory;
import factories.RequestFactory;
import factories.UserFactory;
import models.PrivateRoom;
import models.Request;
import models.User;
import notifications.ChatResponseNotification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.Json;
import services.RequestService;
import services.UserService;
import services.impl.RequestServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 7/6/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestServiceTest {

    private RequestService requestService;

    @Mock
    private RequestDao requestDao;

    @Mock
    private PrivateRoomDao privateRoomDao;

    @Mock
    private UserService userService;

    @Before
    public void setUp() {
        requestService = spy(new RequestServiceImpl(requestDao, privateRoomDao, userService));
    }

    @Test
    public void handleAcceptedResponseNoExistingRoom() throws InstantiationException, IllegalAccessException {
        Request.Status status = Request.Status.accepted;
        long senderId = 1;
        long receiverId = 2;
        User sender = mock(User.class);
        when(sender.userId).thenReturn(senderId);
        User receiver = new UserFactory().create(FieldOverride.of("userId", receiverId));
        when(privateRoomDao.findByRoomMembers(senderId, receiverId)).thenReturn(Optional.empty());

        Request request = new Request(sender, receiver);

        requestService.handleResponse(request, status);

        assertEquals(status, request.status);
        assertTrue(request.respondedTimeStamp > 0);
        verify(userService).sendNotification(refEq(sender), any(ChatResponseNotification.class));
        verify(privateRoomDao).save(argThat(new ArgumentMatcher<PrivateRoom>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof PrivateRoom && ((PrivateRoom) o).request == request;
            }
        }));
    }

    @Test
    public void handleAcceptedResponseWithExistingRoom() throws InstantiationException, IllegalAccessException {
        Request.Status status = Request.Status.accepted;
        long senderId = 1;
        long receiverId = 2;
        User sender = mock(User.class);
        when(sender.userId).thenReturn(senderId);
        User receiver = new UserFactory().create(FieldOverride.of("userId", receiverId));
        PrivateRoom room = new PrivateRoomFactory().create(
                FieldOverride.of("senderInRoom", false),
                FieldOverride.of("receiverInRoom", false));
        when(privateRoomDao.findByRoomMembers(senderId, receiverId)).thenReturn(Optional.of(room));

        Request request = new Request(sender, receiver);

        requestService.handleResponse(request, status);

        assertEquals(status, request.status);
        assertTrue(request.respondedTimeStamp > 0);
        verify(userService).sendNotification(refEq(sender), any(ChatResponseNotification.class));
        verify(privateRoomDao, never()).save(any());
        assertTrue(room.senderInRoom);
        assertTrue(room.receiverInRoom);
    }

    @Test
    public void handleDeniedResponse() throws InstantiationException, IllegalAccessException {
        Request.Status status = Request.Status.denied;
        User mockSender = mock(User.class);
        User receiver = new UserFactory().create();

        Request request = new Request(mockSender, receiver);

        requestService.handleResponse(request, status);

        assertEquals(request.status, status);
        assertTrue(request.respondedTimeStamp > 0);
        verify(userService).sendNotification(refEq(mockSender), any(ChatResponseNotification.class));
        verify(privateRoomDao, never()).save(any(PrivateRoom.class));
    }

    @Test
    public void getStatusPrivateRoomExists() throws InstantiationException, IllegalAccessException {
        long senderId = 1;
        long receiverId = 2;
        long roomId = 3;

        PrivateRoom room = new PrivateRoomFactory().create(FieldOverride.of("roomId", roomId));
        when(privateRoomDao.findByActiveRoomMembers(senderId, receiverId)).thenReturn(Optional.of(room));
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals(status, Json.stringify(Json.toJson(room)));
    }

    @Test
    public void getStatusNoPrivateRoomWithRequestIsSender() throws InstantiationException, IllegalAccessException {
        long senderId = 1;
        long receiverId = 2;
        when(privateRoomDao.findByActiveRoomMembers(senderId, receiverId)).thenReturn(Optional.empty());

        Request.Status requestStatus = Request.Status.denied;
        UserFactory userFactory = new UserFactory();
        User sender = userFactory.create(FieldOverride.of("userId", senderId));
        User receiver = userFactory.create(FieldOverride.of("userId", receiverId));
        Request request = new RequestFactory().create(
                FieldOverride.of("sender", sender),
                FieldOverride.of("receiver", receiver),
                FieldOverride.of("status", requestStatus));
        when(requestService.findByUsers(senderId, receiverId)).thenReturn(Optional.of(request));
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals(requestStatus.name(), status);
    }

    @Test
    public void getStatusNoPrivateRoomWithRequestNotSender() throws InstantiationException, IllegalAccessException {
        long senderId = 1;
        long receiverId = 2;
        when(privateRoomDao.findByActiveRoomMembers(senderId, receiverId)).thenReturn(Optional.empty());

        UserFactory userFactory = new UserFactory();
        User receiver = userFactory.create(FieldOverride.of("userId", senderId));
        User sender = userFactory.create(FieldOverride.of("userId", receiverId));

        Request.Status requestStatus = Request.Status.denied;
        Request request = new RequestFactory().create(
                FieldOverride.of("receiver", receiver),
                FieldOverride.of("sender", sender),
                FieldOverride.of("status", requestStatus));
        when(requestService.findByUsers(senderId, receiverId)).thenReturn(Optional.of(request));
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals("none", status);
    }

    @Test
    public void getStatusNoPrivateRoomWithRequestNotSenderPendingStatus() throws InstantiationException, IllegalAccessException {
        long senderId = 1;
        long receiverId = 2;
        when(privateRoomDao.findByActiveRoomMembers(senderId, receiverId)).thenReturn(Optional.empty());

        UserFactory userFactory = new UserFactory();
        User sender = userFactory.create(FieldOverride.of("userId", senderId));
        User receiver = userFactory.create(FieldOverride.of("userId", receiverId));

        Request.Status requestStatus = Request.Status.pending;
        Request request = new RequestFactory().create(
                FieldOverride.of("receiver", sender),
                FieldOverride.of("sender", receiver),
                FieldOverride.of("status", requestStatus));
        when(requestService.findByUsers(senderId, receiverId)).thenReturn(Optional.of(request));
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals(Request.Status.pending.name(), status);
    }

    @Test
    public void getStatusNoPrivateRoomNoRequest() {
        long senderId = 1;
        long receiverId = 2;
        when(privateRoomDao.findByActiveRoomMembers(senderId, receiverId)).thenReturn(Optional.empty());
        when(requestService.findByUsers(senderId, receiverId)).thenReturn(Optional.empty());
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals("none", status);
    }

    @Test
    public void findPendingRequestsByReceiver() {
        long receiverId = 1;
        List<Request> expectedResults = new ArrayList<>();
        when(requestDao.findPendingRequestsByReceiver(receiverId)).thenReturn(expectedResults);
        List<Request> requests = requestService.findPendingRequestsByReceiver(receiverId);
        verify(requestDao).findPendingRequestsByReceiver(receiverId);
        assertSame(expectedResults, requests);
    }

    @Test
    public void findByUsers() {
        long senderId = 1;
        long receiverId = 2;
        Optional<Request> expected = Optional.empty();
        when(requestDao.findByUsers(senderId, receiverId)).thenReturn(expected);
        Optional<Request> requestOptional = requestService.findByUsers(senderId, receiverId);
        verify(requestDao).findByUsers(senderId, receiverId);
        assertSame(expected, requestOptional);
    }
}
