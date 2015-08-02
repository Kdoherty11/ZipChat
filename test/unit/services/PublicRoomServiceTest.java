package unit.services;

import com.google.common.collect.ImmutableSet;
import daos.PublicRoomDao;
import daos.UserDao;
import factories.DeviceFactory;
import factories.UserFactory;
import models.Device;
import models.PublicRoom;
import models.User;
import notifications.AbstractNotification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.NotificationService;
import services.PublicRoomService;
import services.impl.PublicRoomServiceImpl;
import utils.TestUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 7/6/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class PublicRoomServiceTest {

    private PublicRoomService publicRoomService;

    private UserFactory userFactory;
    private DeviceFactory deviceFactory;

    @Mock
    private PublicRoomDao publicRoomDao;

    @Mock
    private UserDao userDao;

    @Mock
    private NotificationService notificationService;

    @Before
    public void setUp() {
        publicRoomService = spy(new PublicRoomServiceImpl(publicRoomDao, userDao, notificationService));
        userFactory = new UserFactory();
        deviceFactory = new DeviceFactory();
    }

    @Test
    public void sendNotificationNoSubscribers() {
        PublicRoom mockRoom = mock(PublicRoom.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);

        when(publicRoomService.getSubscribers(mockRoom)).thenReturn(Collections.emptySet());

        publicRoomService.sendNotification(mockRoom, mockNotification, Collections.emptySet());

        verifyZeroInteractions(mockRoom, mockNotification);
    }

    @Test
    public void sendNotificationAllUsersInRoom() throws IllegalAccessException, InstantiationException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);

        Set<User> usersInRoom = userFactory.createSet(3);
        Set<Long> userIdsInRoom = usersInRoom.stream().map(user -> user.userId).collect(Collectors.toSet());

        when(publicRoomService.getSubscribers(mockRoom)).thenReturn(usersInRoom);

        publicRoomService.sendNotification(mockRoom, mockNotification, userIdsInRoom);

        verify(notificationService).send(Collections.emptyList(), Collections.emptyList(), mockNotification);
    }

    @Test
    public void sendNotificationUsersNotInRoom() throws IllegalAccessException, InstantiationException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);

        User mockUser1 = mock(User.class);
        User mockUser2 = mock(User.class);

        List<Device> user1Devices = deviceFactory.createList(2, DeviceFactory.Trait.ANDROID);
        List<Device> user2Devices = new ArrayList<>();
        user2Devices.add(deviceFactory.create(DeviceFactory.Trait.ANDROID));
        user2Devices.addAll(deviceFactory.createList(2, DeviceFactory.Trait.IOS));

        when(userDao.getDevices(mockUser1)).thenReturn(user1Devices);
        when(userDao.getDevices(mockUser2)).thenReturn(user2Devices);

        Set<User> subscribers = ImmutableSet.of(mockUser1, mockUser2);
        when(publicRoomService.getSubscribers(mockRoom)).thenReturn(subscribers);

        publicRoomService.sendNotification(mockRoom, mockNotification, Collections.emptySet());

        List<Device> allDevices = new ArrayList<>();
        allDevices.addAll(user1Devices);
        allDevices.addAll(user2Devices);

        List<String> androidRegIds = allDevices.stream()
                .filter(device -> device.platform == Device.Platform.android)
                .map(device -> device.regId)
                .collect(Collectors.toList());

        List<String> iosRegIds = allDevices.stream()
                .filter(device -> device.platform == Device.Platform.ios)
                .map(device -> device.regId)
                .collect(Collectors.toList());

        verify(notificationService).send(androidRegIds, iosRegIds, mockNotification);
    }

    @Test
    public void subscribeAlreadySubscribed() throws IllegalAccessException, InstantiationException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        boolean result = publicRoomService.subscribe(mockRoom, user);

        assertFalse(result);
        assertEquals(1, subscribers.size());
    }

    @Test
    public void subscribeNotYetSubscribed() throws IllegalAccessException, InstantiationException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(mockRoom.subscribers).thenReturn(new HashSet<>());

        boolean result = publicRoomService.subscribe(mockRoom, mock(User.class));

        assertTrue(result);
        assertEquals(1, mockRoom.subscribers.size());
    }

    @Test
    public void unsubscribeIsSubscribed() throws InstantiationException, IllegalAccessException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        User unsubscribingUser = userFactory.create(UserFactory.Trait.UNIQUE_NAME);

        boolean result = publicRoomService.unsubscribe(mockRoom, unsubscribingUser);

        assertFalse(result);
        assertEquals(1, subscribers.size());
    }

    @Test
    public void unsubscribeNotSubscribed() throws InstantiationException, IllegalAccessException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        boolean result = publicRoomService.unsubscribe(mockRoom, user);

        assertTrue(result);
        assertTrue(subscribers.isEmpty());
    }

    @Test
    public void isSubscribedTrue() throws InstantiationException, IllegalAccessException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        boolean isSubscribed = publicRoomService.isSubscribed(mockRoom, user.userId);

        assertTrue(isSubscribed);
    }

    @Test
    public void isSubscribedFalse() throws InstantiationException, IllegalAccessException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        boolean isSubscribed = publicRoomService.isSubscribed(mockRoom, TestUtils.getUniqueId(user));

        assertFalse(isSubscribed);
    }

    @Test
    public void allInGeoRange() {
        double lat = 1.0;
        double lon = 2.0;
        List<PublicRoom> expected = new ArrayList<>();
        when(publicRoomDao.allInGeoRange(lat, lon)).thenReturn(expected);
        List<PublicRoom> actual = publicRoomService.allInGeoRange(lat, lon);

        assertSame(expected, actual);
    }

    @Test
    public void getSubscribers() {
        PublicRoom mockRoom = mock(PublicRoom.class);
        Set<User> expected = new HashSet<>();
        when(publicRoomDao.getSubscribers(mockRoom)).thenReturn(expected);
        Set<User> actual = publicRoomService.getSubscribers(mockRoom);

        assertSame(expected, actual);
    }


}
