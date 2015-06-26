package unit;

import com.google.common.collect.ImmutableMap;
import factories.IncludeEntity;
import factories.ObjectFactory;
import integration.AbstractTest;
import models.Platform;
import models.entities.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by kevin on 6/22/15.
 */
@Ignore
public class FactoryTest extends AbstractTest {

    @Test
    public void createUser() throws Throwable {
        ObjectFactory<User> userObjectFactory = new ObjectFactory<>(User.class);
        User user = userObjectFactory.create();
        testPersistedUser(user);
        assertThat(user.devices).isNull();
        userObjectFactory.cleanUp();
    }

    @Test
    public void createUserWithDevices() throws Throwable {
        int numDevices = 2;
        Map<String, Object> deviceOverrides = new HashMap<>();
        deviceOverrides.put("regId", "KevinDoherty");
        ObjectFactory<User> userObjectFactory = new ObjectFactory<>(User.class);
        User user = userObjectFactory.create(ImmutableMap.of("devices", new IncludeEntity<>(Device.class, 2, deviceOverrides)));
        testPersistedUser(user);
        assertThat(user.devices).hasSize(numDevices);
        assertThat(user.devices.get(0).user).isEqualTo(user);
        assertThat(user.devices.get(0).deviceId).isPositive();
        userObjectFactory.cleanUp();
    }

    @Test
    public void createUserOverridingDevices() throws Throwable {
        ObjectFactory<User> userFactory = new ObjectFactory<>(User.class);
        User user = userFactory.create();

        ObjectFactory<Device> deviceFactory = new ObjectFactory<>(Device.class);
        List<Device> androidDevices = deviceFactory.createList(3, ImmutableMap.of("platform", Platform.android, "user", user));
        List<Device> iosDevices = deviceFactory.createList(2, ImmutableMap.of("platform", Platform.ios, "user", user));
        List<Device> allDevices = new ArrayList<>();
        allDevices.addAll(androidDevices);
        allDevices.addAll(iosDevices);

        user.devices = allDevices;

        testPersistedUser(user);
        assertThat(user.devices).hasSize(allDevices.size());
        testDevice(user.devices.get(0));
    }

    @Test
    public void createRequest() throws Throwable {
        ObjectFactory<Request> factory = new ObjectFactory<>(Request.class);
        Request request = factory.create();
        testPersistedRequest(request);
        factory.cleanUp();
    }

    @Test
    public void createPrivateRoomWithRequest() throws Throwable {
        ObjectFactory<PrivateRoom> factory = new ObjectFactory<>(PrivateRoom.class);
        PrivateRoom room = factory.create(ImmutableMap.of("request", new IncludeEntity<>(Request.class)));
        testPersistedPrivateRoom(room);
        testPersistedRequest(room.request);
        factory.cleanUp();
    }

    @Test
    public void createPrivateRoom() throws Throwable {
        ObjectFactory<PrivateRoom> factory = new ObjectFactory<>(PrivateRoom.class);
        PrivateRoom room = factory.create();
        testPersistedPrivateRoom(room);
        factory.cleanUp();
    }

    @Test
    public void createPrivateRoomWithMessages() throws Throwable {
        ObjectFactory<PrivateRoom> factory = new ObjectFactory<>(PrivateRoom.class);
        int numMessages = 2;
        PrivateRoom room = factory.create(ImmutableMap.of("messages", new IncludeEntity<>(Message.class, numMessages)));
        testPersistedPrivateRoom(room);
        assertThat(room.messages.get(0).room).isEqualTo(room);
        assertThat(room.messages.get(0).messageId).isPositive();
        factory.cleanUp();
    }

    @Test
    public void createPublicRoom() throws Throwable {
        ObjectFactory<PublicRoom> factory = new ObjectFactory<>(PublicRoom.class);
        PublicRoom room = factory.create();
        testPersistedPublicRoom(room);
        factory.cleanUp();
    }

    @Test
    public void createPublicRoomWithMessages() throws Throwable {
        ObjectFactory<PublicRoom> factory = new ObjectFactory<>(PublicRoom.class);
        int numMessages = 2;
        PublicRoom room = factory.create(ImmutableMap.of("messages", new IncludeEntity<>(Message.class, numMessages)));
        testPersistedPublicRoom(room);
        assertThat(room.messages).hasSize(numMessages);
        assertThat(room.messages.get(0).room).isEqualTo(room);
        assertThat(room.messages.get(0).messageId).isPositive();
        factory.cleanUp();
    }

    @Test
    public void createPublicRoomWithSubscribers() throws Throwable {
        ObjectFactory<PublicRoom> factory = new ObjectFactory<>(PublicRoom.class);
        int numSubscribers = 2;
        PublicRoom room = factory.create(ImmutableMap.of("subscribers", new IncludeEntity<>(User.class, numSubscribers)));
        testPersistedPublicRoom(room);
        assertThat(room.subscribers).hasSize(numSubscribers);
        factory.cleanUp();
    }

    @Test
    public void createPublicRoomWithAnonUsers() throws Throwable {
        ObjectFactory<PublicRoom> factory = new ObjectFactory<>(PublicRoom.class);
        int numAnonUsers = 2;
        PublicRoom room = factory.create(ImmutableMap.of("anonUsers", new IncludeEntity<>(AnonUser.class, numAnonUsers)));
        testPersistedPublicRoom(room);
        assertThat(room.anonUsers).hasSize(numAnonUsers);
        assertThat(room.anonUsers.get(0).room).isEqualTo(room);
        factory.cleanUp();
    }

    @Test
    public void createAnonUsers() throws Throwable {
        ObjectFactory<AnonUser> factory = new ObjectFactory<>(AnonUser.class);

        AnonUser anonUser = factory.create();
        testAnonUser(anonUser);
        // TODO: Room doesn't have anonUser in room.anonUsers

        factory.cleanUp();
    }

    @Test
    public void createDevice() throws Throwable {
        ObjectFactory<Device> factory = new ObjectFactory<>(Device.class);

        Device device = factory.create();
        testDevice(device);

        factory.cleanUp();
    }

    @Test
    public void createMessage() throws Throwable {
        ObjectFactory<Message> factory = new ObjectFactory<>(Message.class);

        Message message = factory.create();
        testMessage(message);

        factory.cleanUp();
    }

    @Test
    public void createMessageWithFavorites() throws Throwable {
        ObjectFactory<Message> factory = new ObjectFactory<>(Message.class);

        int numFavorites = 2;
        Message message = factory.create(ImmutableMap.of("favorites", new IncludeEntity<>(User.class, numFavorites)));
        testMessage(message);
        assertThat(message.favorites).hasSize(numFavorites);

        factory.cleanUp();
    }

    @Test
    public void createMessageWithFlags() throws Throwable {
        ObjectFactory<Message> factory = new ObjectFactory<>(Message.class);

        int numFavorites = 2;
        Message message = factory.create(ImmutableMap.of("flags", new IncludeEntity<>(User.class, numFavorites)));
        testMessage(message);
        assertThat(message.flags).hasSize(numFavorites);

        factory.cleanUp();
    }
}
