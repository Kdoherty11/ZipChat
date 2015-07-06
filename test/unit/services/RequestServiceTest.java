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

import static org.fest.assertions.Assertions.assertThat;
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

        assertThat(request.status).isEqualTo(status);
        assertThat(request.respondedTimeStamp).isPositive();
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

        assertThat(request.status).isEqualTo(status);
        assertThat(request.respondedTimeStamp).isPositive();
        verify(userService).sendNotification(refEq(mockSender), any(ChatResponseNotification.class));
        verify(privateRoomDao, never()).save(any(PrivateRoom.class));
    }


}
