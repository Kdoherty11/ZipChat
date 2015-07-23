package unit.services;

import com.fasterxml.jackson.databind.JsonNode;
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
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.NotificationService;
import services.UserService;
import services.impl.UserServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
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

    @Mock
    private NotificationService notificationService;

    private DeviceFactory deviceFactory;

    @Before
    public void setUp() {
        userService = spy(new UserServiceImpl(userDao, requestDao, privateRoomDao, wsClient, notificationService));
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

        userService.sendNotification(mockReceiver, mockNotification);

        verifyZeroInteractions(mockNotification);
    }

    @Test
    public void sendSingleAndroidNotification() throws Throwable {
        User mockReceiver = mock(User.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        doNothing().when(notificationService).send(anyListOf(String.class), anyListOf(String.class), refEq(mockNotification));

        List<Device> devices = deviceFactory.createList(1, PropOverride.of("platform", Platform.android));
        when(userDao.getDevices(mockReceiver)).thenReturn(devices);


        userService.sendNotification(mockReceiver, mockNotification);

        verify(notificationService).send(Collections.singletonList(devices.get(0).regId), Collections.emptyList(), mockNotification);
    }

    @Test
    public void sendSingleIosNotification() throws Throwable {
        User mockReceiver = mock(User.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        doNothing().when(notificationService).send(anyListOf(String.class), anyListOf(String.class), refEq(mockNotification));

        List<Device> devices = deviceFactory.createList(1, PropOverride.of("platform", Platform.ios));
        when(userDao.getDevices(mockReceiver)).thenReturn(devices);

        userService.sendNotification(mockReceiver, mockNotification);

        verify(notificationService).send(Collections.emptyList(), Collections.singletonList(devices.get(0).regId), mockNotification);
    }

    @Test
    public void sendNotificationBoth() throws Throwable {
        User mockReceiver = mock(User.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        doNothing().when(notificationService).send(anyListOf(String.class), anyListOf(String.class), refEq(mockNotification));

        List<Device> androidDevices = deviceFactory.createList(3, DeviceFactory.Trait.ANDROID);
        List<Device> iosDevices = deviceFactory.createList(2, DeviceFactory.Trait.IOS);
        List<Device> devices = new ArrayList<>();
        devices.addAll(androidDevices);
        devices.addAll(iosDevices);

        when(userDao.getDevices(mockReceiver)).thenReturn(devices);

        userService.sendNotification(mockReceiver, mockNotification);

        List<String> androidRegIds = androidDevices.stream().map(device -> device.regId).collect(Collectors.toList());
        List<String> iosRegIds = iosDevices.stream().map(device -> device.regId).collect(Collectors.toList());

        verify(notificationService).send(androidRegIds, iosRegIds, mockNotification);
    }

    @Test
    public void testGetFacebookInformation() {
        String fbAccessToken = "testFbAccessToken";
        WSRequest mockRequestHolder = mock(WSRequest.class);
        WSRequest mockSecondRequestHolder = mock(WSRequest.class);
        WSResponse mockResponse = mock(WSResponse.class);
        @SuppressWarnings("unchecked") F.Promise<WSResponse> mockResponsePromise =
                (F.Promise<WSResponse>) mock(F.Promise.class);

        when(wsClient.url("https://graph.facebook.com/me")).thenReturn(mockRequestHolder);
        when(mockRequestHolder.setQueryParameter("access_token", fbAccessToken)).thenReturn(mockSecondRequestHolder);
        when(mockSecondRequestHolder.get()).thenReturn(mockResponsePromise);
        when(mockResponsePromise.get(anyLong(), any(TimeUnit.class))).thenReturn(mockResponse);

        JsonNode expectedResult = Json.newObject();
        when(mockResponse.asJson()).thenReturn(expectedResult);

        JsonNode response = userService.getFacebookInformation(fbAccessToken);
        assertSame(expectedResult, response);
    }

    @Test
    public void findById() {
        long userId = 1;
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        Optional<User> userOptional = userService.findById(userId);
        assertFalse(userOptional.isPresent());

        verify(userDao).findById(userId);
    }

    @Test
    public void findByFacebookId() {
        String facebookId = "TestFbId";
        when(userDao.findByFacebookId(facebookId)).thenReturn(Optional.empty());

        Optional<User> userOptional = userService.findByFacebookId(facebookId);

        verify(userDao).findByFacebookId(facebookId);
        assertFalse(userOptional.isPresent());
    }

    @Test
    public void getDevices() {
        List<Device> expected = new ArrayList<>();
        User mockUser = mock(User.class);
        when(userDao.getDevices(mockUser)).thenReturn(expected);

        List<Device> actual = userService.getDevices(mockUser);

        assertSame(expected, actual);
    }

}
