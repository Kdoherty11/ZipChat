package unit.services;

import daos.PrivateRoomDao;
import daos.RequestDao;
import factories.FieldOverride;
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
    public void handleAcceptedResponse() throws InstantiationException, IllegalAccessException {
        Request.Status status = Request.Status.accepted;
        User mockSender = mock(User.class);
        User receiver = new UserFactory().create();

        Request request = new Request(mockSender, receiver);

        requestService.handleResponse(request, status);

        assertEquals(status, request.status);
        assertTrue(request.respondedTimeStamp > 0);
        verify(userService).sendNotification(refEq(mockSender), any(ChatResponseNotification.class));
        verify(privateRoomDao).save(argThat(new ArgumentMatcher<PrivateRoom>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof PrivateRoom && ((PrivateRoom) o).request == request;
            }
        }));
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
    public void getStatusPrivateRoomExists() {
        long senderId = 1;
        long receiverId = 2;
        long roomId = 3;
        PrivateRoom mockRoom = mock(PrivateRoom.class);
        when(mockRoom.roomId).thenReturn(roomId);
        when(privateRoomDao.findByRoomMembers(senderId, receiverId)).thenReturn(Optional.of(mockRoom));
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals(status, Long.toString(roomId));
    }

    @Test
    public void getStatusNoPrivateRoomWithRequest() {
        long senderId = 1;
        long receiverId = 2;
        when(privateRoomDao.findByRoomMembers(senderId, receiverId)).thenReturn(Optional.empty());

        Request mockRequest = mock(Request.class);
        Request.Status requestStatus = Request.Status.denied;
        when(mockRequest.status).thenReturn(requestStatus);
        when(requestService.findBySenderAndReceiver(senderId, receiverId)).thenReturn(Optional.of(mockRequest));
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals(status, requestStatus.name());
    }

    @Test
    public void getStatusNoPrivateRoomNoRequest() {
        long senderId = 1;
        long receiverId = 2;
        when(privateRoomDao.findByRoomMembers(senderId, receiverId)).thenReturn(Optional.empty());
        when(requestService.findBySenderAndReceiver(senderId, receiverId)).thenReturn(Optional.empty());
        when(requestService.findBySenderAndReceiver(receiverId, senderId)).thenReturn(Optional.empty());
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals(status, "none");
    }

    @Test
    public void getStatusNoPrivateRoomWithOppositeRequest() throws InstantiationException, IllegalAccessException {
        long senderId = 1;
        long receiverId = 2;
        Request request = new RequestFactory().create(FieldOverride.of("status", Request.Status.pending));
        when(privateRoomDao.findByRoomMembers(senderId, receiverId)).thenReturn(Optional.empty());
        when(requestService.findBySenderAndReceiver(senderId, receiverId)).thenReturn(Optional.empty());
        when(requestService.findBySenderAndReceiver(receiverId, senderId)).thenReturn(Optional.of(request));
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals(status, Request.Status.pending.name());
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
    public void findBySenderAndReceiver() {
        long senderId = 1;
        long receiverId = 2;
        Optional<Request> expected = Optional.empty();
        when(requestDao.findBySenderAndReceiver(senderId, receiverId)).thenReturn(expected);
        Optional<Request> requestOptional = requestService.findBySenderAndReceiver(senderId, receiverId);
        verify(requestDao).findBySenderAndReceiver(senderId, receiverId);
        assertSame(expected, requestOptional);
    }
}
