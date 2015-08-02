package unit.services;

import daos.PrivateRoomDao;
import daos.RequestDao;
import daos.UserDao;
import exceptions.MethodShouldNotBeCalled;
import models.AbstractUser;
import models.PrivateRoom;
import models.Request;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Logger;
import services.UserService;
import services.impl.UserServiceImpl;

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
    private UserDao userDao;

    @Mock
    private RequestDao requestDao;

    @Mock
    private PrivateRoomDao privateRoomDao;

    @Before
    public void setUp() {
        userService = new UserServiceImpl(userDao, requestDao, privateRoomDao);
    }

    @Test
    public void sendChatRequest() {
        Logger.debug("Send chat request test!!!");
        AbstractUser mockReceiver = mock(AbstractUser.class);
        User mockActualReceiver = mock(User.class);
        when(mockReceiver.getActual()).thenReturn(mockActualReceiver);

        User mockSender = mock(User.class);
        when(mockSender.name).thenReturn("TestName");
        when(mockSender.facebookId).thenReturn("TestFbId");

        when(privateRoomDao.findByRoomMembers(anyLong(), anyLong())).thenReturn(Optional.empty());

        doNothing().when(requestDao).save(any(Request.class));
        // TODO doNothing().when(mockActualReceiver).sendNotification(any(ChatRequestNotification.class));

        spy(requestDao);
        spy(mockActualReceiver);

        userService.sendChatRequest(mockSender, mockReceiver);

        // TODO verify(mockActualReceiver).sendNotification(isA(ChatRequestNotification.class));
        verify(requestDao).save(any(Request.class));
    }

    @Test
    public void sendChatRequestRoomAlreadyExists() {
        AbstractUser mockReceiver = mock(AbstractUser.class);
        User mockActualReceiver = mock(User.class);
        when(mockReceiver.getActual()).thenReturn(mockActualReceiver);

        User mockSender = mock(User.class);
        when(mockSender.name).thenReturn("TestName");
        when(mockSender.facebookId).thenReturn("TestFbId");

        when(privateRoomDao.findByRoomMembers(anyLong(), anyLong())).thenReturn(Optional.of(new PrivateRoom()));

        doThrow(MethodShouldNotBeCalled.class).when(requestDao).save(any());
        // TODO doThrow(MethodShouldNotBeCalled.class).when(mockActualReceiver).sendNotification(any());

        userService.sendChatRequest(mockSender, mockReceiver);
    }

    @Test
    public void findById() {
        long userId = 1;
        when(userDao.findById(userId)).thenReturn(Optional.empty());
        spy(userDao);

        userService.findById(userId);

        verify(userDao).findById(userId);
    }

    @Test
    public void findByFacebookId() {
        String facebookId = "TestFbId";
        when(userDao.findByFacebookId(facebookId)).thenReturn(Optional.empty());
        spy(userDao);

        userService.findByFacebookId(facebookId);

        verify(userDao).findByFacebookId(facebookId);
    }
}
