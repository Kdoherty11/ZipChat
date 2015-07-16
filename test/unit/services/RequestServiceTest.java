package unit.services;

import daos.PrivateRoomDao;
import daos.RequestDao;
import models.entities.PrivateRoom;
import models.entities.Request;
import models.entities.User;
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

import static org.fest.assertions.Assertions.assertEquals;
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
    public void handleAcceptedResponse() {
        Request.Status status = Request.Status.accepted;
        User mockSender = mock(User.class);
        User mockReceiver = mock(User.class);
        when(mockReceiver.name).thenReturn("John Doe");

        Request request = new Request(mockSender, mockReceiver);

        requestService.handleResponse(request, status);

        assertEquals(request.status).isEqualTo(status);
        assertEquals(request.respondedTimeStamp).isPositive();
        verify(userService).sendNotification(refEq(mockSender), any(ChatResponseNotification.class));
        verify(privateRoomDao).save(argThat(new ArgumentMatcher<PrivateRoom>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof PrivateRoom && ((PrivateRoom) o).request == request;
            }
        }));
    }

    @Test
    public void handleDeniedResponse() {
        Request.Status status = Request.Status.denied;
        User mockSender = mock(User.class);
        User mockReceiver = mock(User.class);
        when(mockReceiver.name).thenReturn("John Doe");

        Request request = new Request(mockSender, mockReceiver);

        requestService.handleResponse(request, status);

        assertEquals(request.status).isEqualTo(status);
        assertEquals(request.respondedTimeStamp).isPositive();
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
        when(privateRoomDao.findBySenderAndReceiver(senderId, receiverId)).thenReturn(Optional.of(mockRoom));
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals(status).isEqualTo(Long.toString(roomId));
    }

    @Test
    public void getStatusNoPrivateRoomWithRequest() {
        long senderId = 1;
        long receiverId = 2;
        when(privateRoomDao.findBySenderAndReceiver(senderId, receiverId)).thenReturn(Optional.empty());

        Request mockRequest = mock(Request.class);
        Request.Status requestStatus = Request.Status.denied;
        when(mockRequest.status).thenReturn(requestStatus);
        when(requestService.findBySenderAndReceiver(senderId, receiverId)).thenReturn(Optional.of(mockRequest));
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals(status).isEqualTo(requestStatus.name());
    }

    @Test
    public void getStatusNoPrivateRoomNoRequest() {
        long senderId = 1;
        long receiverId = 2;
        when(privateRoomDao.findBySenderAndReceiver(senderId, receiverId)).thenReturn(Optional.empty());
        when(requestService.findBySenderAndReceiver(senderId, receiverId)).thenReturn(Optional.empty());
        String status = requestService.getStatus(senderId, receiverId);

        assertEquals(status).isEqualTo("none");
    }

    @Test
    public void findPendingRequestsByReceiver() {
        long receiverId = 1;
        List<Request> expectedResults = new ArrayList<>();
        when(requestDao.findPendingRequestsByReceiver(receiverId)).thenReturn(expectedResults);
        List<Request> requests = requestService.findPendingRequestsByReceiver(receiverId);
        verify(requestDao).findPendingRequestsByReceiver(receiverId);
        assertEquals(requests == expectedResults).isTrue();
    }

    @Test
    public void findBySenderAndReceiver() {
        long senderId = 1;
        long receiverId = 2;
        Optional<Request> expected = Optional.empty();
        when(requestDao.findBySenderAndReceiver(senderId, receiverId)).thenReturn(expected);
        Optional<Request> requestOptional = requestService.findBySenderAndReceiver(senderId, receiverId);
        verify(requestDao).findBySenderAndReceiver(senderId, receiverId);
        assertEquals(requestOptional == expected).isTrue();
    }
}
