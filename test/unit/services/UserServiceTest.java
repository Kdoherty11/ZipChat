package unit.services;

import daos.PrivateRoomDao;
import daos.RequestDao;
import daos.UserDao;
import factories.DeviceFactory;
import factories.PropOverride;
import models.Platform;
import models.entities.*;
import notifications.AbstractNotification;
import notifications.ChatRequestNotification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.ws.WSResponse;
import play.libs.F;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequestHolder;
import services.UserService;
import services.impl.UserServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.fest.assertions.Assertions.assertThat;
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

    @Mock
    private WSClient wsClient;

    private DeviceFactory deviceFactory;

    @Before
    public void setUp() {
        userService = spy(new UserServiceImpl(userDao, requestDao, privateRoomDao, wsClient));
        deviceFactory = new DeviceFactory();
    }

    @Test
    public void sendChatRequest() {
        AbstractUser mockReceiver = mock(AbstractUser.class);
        User mockActualReceiver = mock(User.class);
        when(mockReceiver.getActual()).thenReturn(mockActualReceiver);

        User mockSender = mock(User.class);
        when(mockSender.name).thenReturn("TestName");
        when(mockSender.facebookId).thenReturn("TestFbId");

        when(privateRoomDao.findBySenderAndReceiver(anyLong(), anyLong())).thenReturn(Optional.empty());

        doNothing().when(requestDao).save(any(Request.class));
        doNothing().when(userService).sendNotification(refEq(mockActualReceiver), any(ChatRequestNotification.class));

        userService.sendChatRequest(mockSender, mockReceiver);

        verify(requestDao).save(any(Request.class));
        verify(userService).sendNotification(refEq(mockActualReceiver), any(ChatRequestNotification.class));
    }

    @Test
    public void sendChatRequestRoomAlreadyExists() {
        AbstractUser mockReceiver = mock(AbstractUser.class);
        User mockActualReceiver = mock(User.class);
        when(mockReceiver.getActual()).thenReturn(mockActualReceiver);

        User mockSender = mock(User.class);
        when(mockSender.name).thenReturn("TestName");
        when(mockSender.facebookId).thenReturn("TestFbId");

        when(privateRoomDao.findBySenderAndReceiver(anyLong(), anyLong())).thenReturn(Optional.of(new PrivateRoom()));

        verify(requestDao, never()).save(any());
        verify(userService, never()).sendNotification(any(), any());

        userService.sendChatRequest(mockSender, mockReceiver);
    }

    @Test
    public void sendNotificationNoDevices() throws Throwable {
        User mockReceiver = mock(User.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);

        List<Device> devices = new ArrayList<>();
        when(userDao.getDevices(mockReceiver)).thenReturn(devices);

        verifyZeroInteractions(mockNotification);
    }

    @Test
    public void sendSingleAndroidNotification() throws Throwable {
        User mockReceiver = mock(User.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        doNothing().when(mockNotification).send(anyListOf(String.class), anyListOf(String.class));

        List<Device> devices = deviceFactory.createList(1, PropOverride.of("platform", Platform.android));
        when(userDao.getDevices(mockReceiver)).thenReturn(devices);


        userService.sendNotification(mockReceiver, mockNotification);

        verify(mockNotification).send(Collections.singletonList(devices.get(0).regId), Collections.emptyList());
    }

    @Test
    public void sendSingleIosNotification() throws Throwable {
        User mockReceiver = mock(User.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        doNothing().when(mockNotification).send(anyListOf(String.class), anyListOf(String.class));

        List<Device> devices = deviceFactory.createList(1, PropOverride.of("platform", Platform.ios));
        when(userDao.getDevices(mockReceiver)).thenReturn(devices);

        userService.sendNotification(mockReceiver, mockNotification);

        verify(mockNotification).send(Collections.emptyList(), Collections.singletonList(devices.get(0).regId));
    }
//
    @Test
    public void sendNotificationBoth() throws Throwable {
        User mockReceiver = mock(User.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        doNothing().when(mockNotification).send(anyListOf(String.class), anyListOf(String.class));

        List<Device> androidDevices = deviceFactory.createList(3, PropOverride.of("platform", Platform.android));
        List<Device> iosDevices = deviceFactory.createList(2, PropOverride.of("platform", Platform.ios));
        List<Device> devices = new ArrayList<>();
        devices.addAll(androidDevices);
        devices.addAll(iosDevices);

        when(userDao.getDevices(mockReceiver)).thenReturn(devices);

        userService.sendNotification(mockReceiver, mockNotification);

        List<String> androidRegIds = androidDevices.stream().map(device -> device.regId).collect(Collectors.toList());
        List<String> iosRegIds = iosDevices.stream().map(device -> device.regId).collect(Collectors.toList());

        verify(mockNotification).send(androidRegIds, iosRegIds);
    }

    @Test
    public void testGetFacebookInformation() {
        String fbAccessToken = "testFbAccessToken";
        WSClient mockWs = mock(WSClient.class);
        WSRequestHolder mockRequestHolder = mock(WSRequestHolder.class);
        WSRequestHolder mockSecondRequestHolder = mock(WSRequestHolder.class);

        @SuppressWarnings("unchecked")
        F.Promise<WSResponse> mockResponsePromise = (F.Promise<WSResponse>) mock(F.Promise.class);

        when(mockWs.url("https://graph.facebook.com/me")).thenReturn(mockRequestHolder);
        when(mockRequestHolder.setQueryParameter("access_token", fbAccessToken)).thenReturn(mockSecondRequestHolder);
        when(mockSecondRequestHolder.get()).thenReturn(mockResponsePromise);
    }

    @Test
    public void findById() {
        long userId = 1;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        Optional<User> userOptional = userService.findById(userId);
        assertThat(userOptional.isPresent()).isFalse();;

        verify(userDao).findById(userId);
    }

    @Test
    public void findByFacebookId() {
        String facebookId = "TestFbId";
        when(userDao.findByFacebookId(facebookId)).thenReturn(Optional.empty());

        Optional<User> userOptional = userService.findByFacebookId(facebookId);

        verify(userDao).findByFacebookId(facebookId);
        assertThat(userOptional.isPresent()).isFalse();;
    }

}
