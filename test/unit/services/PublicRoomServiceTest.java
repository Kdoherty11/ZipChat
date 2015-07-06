package unit.services;

import com.google.common.collect.ImmutableSet;
import daos.PublicRoomDao;
import daos.UserDao;
import factories.DeviceFactory;
import factories.PropOverride;
import factories.UserFactory;
import models.Platform;
import models.entities.Device;
import models.entities.PublicRoom;
import models.entities.Request;
import models.entities.User;
import notifications.AbstractNotification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.PublicRoomService;
import services.impl.PublicRoomServiceImpl;
import utils.TestUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.fest.assertions.Assertions.assertThat;
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

    @Before
    public void setUp() {
        publicRoomService = spy(new PublicRoomServiceImpl(publicRoomDao, userDao));
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

        verify(mockNotification).send(Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void sendNotificationUsersNotInRoom() throws IllegalAccessException, InstantiationException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        AbstractNotification mockNotification = mock(AbstractNotification.class);

        User mockUser1 = mock(User.class);
        User mockUser2 = mock(User.class);

        List<Device> user1Devices = deviceFactory.createList(2, PropOverride.of("platform", Platform.android));
        List<Device> user2Devices = new ArrayList<>();
        user2Devices.add(deviceFactory.create(PropOverride.of("platform", Platform.ios)));
        user2Devices.addAll(deviceFactory.createList(2, PropOverride.of("platform", Platform.android)));

        when(userDao.getDevices(mockUser1)).thenReturn(user1Devices);
        when(userDao.getDevices(mockUser2)).thenReturn(user2Devices);

        Set<User> subscribers = ImmutableSet.of(mockUser1, mockUser2);
        when(publicRoomService.getSubscribers(mockRoom)).thenReturn(subscribers);

        publicRoomService.sendNotification(mockRoom, mockNotification, Collections.emptySet());

        List<Device> allDevices = new ArrayList<>();
        allDevices.addAll(user1Devices);
        allDevices.addAll(user2Devices);

        List<String> androidRegIds = allDevices.stream()
                .filter(device -> device.platform == Platform.android)
                .map(device -> device.regId)
                .collect(Collectors.toList());

        List<String> iosRegIds = allDevices.stream()
                .filter(device -> device.platform == Platform.ios)
                .map(device -> device.regId)
                .collect(Collectors.toList());


        verify(mockNotification).send(androidRegIds, iosRegIds);
    }

    @Test
    public void subscribeAlreadySubscribed() throws IllegalAccessException, InstantiationException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        boolean result = publicRoomService.subscribe(mockRoom, user);

        assertThat(result).isFalse();
        assertThat(subscribers).hasSize(1);
    }

    @Test
    public void subscribeNotYetSubscribed() throws IllegalAccessException, InstantiationException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        User subscribeUser = userFactory.create(PropOverride.of("name", UUID.randomUUID().toString()));

        boolean result = publicRoomService.subscribe(mockRoom, subscribeUser);

        assertThat(result).isTrue();
        assertThat(subscribers).hasSize(2);
    }

    @Test
    public void unsubscribeIsSubscribed() throws InstantiationException, IllegalAccessException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        User unsubscribingUser = userFactory.create(PropOverride.of("name", UUID.randomUUID().toString()));

        boolean result = publicRoomService.unsubscribe(mockRoom, unsubscribingUser);

        assertThat(result).isFalse();
        assertThat(subscribers).hasSize(1);
    }

    @Test
    public void unsubscribeNotSubscribed() throws InstantiationException, IllegalAccessException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        boolean result = publicRoomService.unsubscribe(mockRoom, user);

        assertThat(result).isTrue();
        assertThat(subscribers).isEmpty();
    }

    @Test
    public void isSubscribedTrue() throws InstantiationException, IllegalAccessException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        boolean isSubscribed = publicRoomService.isSubscribed(mockRoom, user.userId);

        assertThat(isSubscribed).isTrue();
    }

    @Test
    public void isSubscribedFalse() throws InstantiationException, IllegalAccessException {
        PublicRoom mockRoom = mock(PublicRoom.class);
        User user = userFactory.create();
        Set<User> subscribers = TestUtils.setOf(user);
        when(mockRoom.subscribers).thenReturn(subscribers);

        boolean isSubscribed = publicRoomService.isSubscribed(mockRoom, TestUtils.getUniqueId(user));

        assertThat(isSubscribed).isFalse();
    }

    @Test
    public void findBySenderAndReceiver() {
        long senderId = 1;
        long receiverId = 2;
        Optional<Request> expected = Optional.empty();
        when(requestDao.findBySenderAndReceiver(senderId, receiverId)).thenReturn(expected);
        Optional<Request> requestOptional = requestService.findBySenderAndReceiver(senderId, receiverId);
        verify(requestDao).findBySenderAndReceiver(senderId, receiverId);
        assertThat(requestOptional == expected).isTrue();
    }

    @Test
    public void allInGeoRange() {
        double lat = 1.0;
        double lon = 2.0;
        List<PublicRoom> expected = new ArrayList<>();
        when(publicRoomDao.allInGeoRange(lat, lon)).thenReturn(expected);
        List<PublicRoom> actual = publicRoomService.allInGeoRange(lat, lon);

        assertThat(actual == expected).isTrue();
    }


}
