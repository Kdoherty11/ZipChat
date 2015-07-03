package services.services;

import exceptions.MethodShouldNotBeCalled;
import models.entities.AbstractUser;
import models.entities.PrivateRoom;
import models.entities.Request;
import models.entities.User;
import notifications.ChatRequestNotification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import repositories.PrivateRoomRepository;
import repositories.RequestRepository;
import repositories.UserRepository;
import services.UserService;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 7/2/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private PrivateRoomRepository privateRoomRepository;

    @Before
    public void setUp() {
        userService = new UserService(userRepository, requestRepository, privateRoomRepository);
    }

    @Test
    public void sendChatRequest() {
        AbstractUser mockReceiver = mock(AbstractUser.class);
        User mockActualReceiver = mock(User.class);
        when(mockReceiver.getActual()).thenReturn(mockActualReceiver);

        User mockSender = mock(User.class);
        when(mockSender.name).thenReturn("TestName");
        when(mockSender.facebookId).thenReturn("TestFbId");

        when(privateRoomRepository.findBySenderAndReceiver(anyLong(), anyLong())).thenReturn(Optional.empty());

        doNothing().when(requestRepository).save(any(Request.class));
        doNothing().when(mockActualReceiver).sendNotification(any(ChatRequestNotification.class));

        spy(requestRepository);
        spy(mockActualReceiver);

        userService.sendChatRequest(mockSender, mockReceiver);

        verify(mockActualReceiver).sendNotification(isA(ChatRequestNotification.class));
        verify(requestRepository).save(any(Request.class));
    }

    @Test
    public void sendChatRequestRoomAlreadyExists() {
        AbstractUser mockReceiver = mock(AbstractUser.class);
        User mockActualReceiver = mock(User.class);
        when(mockReceiver.getActual()).thenReturn(mockActualReceiver);

        User mockSender = mock(User.class);
        when(mockSender.name).thenReturn("TestName");
        when(mockSender.facebookId).thenReturn("TestFbId");

        when(privateRoomRepository.findBySenderAndReceiver(anyLong(), anyLong())).thenReturn(Optional.of(new PrivateRoom()));

        doThrow(MethodShouldNotBeCalled.class).when(requestRepository).save(any());
        doThrow(MethodShouldNotBeCalled.class).when(mockActualReceiver).sendNotification(any());

        userService.sendChatRequest(mockSender, mockReceiver);
    }

    @Test
    public void findById() {
        long userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        spy(userRepository);

        userService.findById(userId);

        verify(userRepository).findById(userId);
    }

    @Test
    public void findByFacebookId() {
        String facebookId = "TestFbId";
        when(userRepository.findByFacebookId(facebookId)).thenReturn(Optional.empty());
        spy(userRepository);

        userService.findByFacebookId(facebookId);

        verify(userRepository).findByFacebookId(facebookId);
    }
}
