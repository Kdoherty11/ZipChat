package unit.models;

import com.google.common.collect.ImmutableMap;
import factories.IncludeEntity;
import factories.ObjectFactory;
import integration.AbstractTest;
import models.Platform;
import models.entities.Device;
import models.entities.User;
import notifications.AbstractNotification;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.db.jpa.JPA;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by kevin on 6/21/15.
 */
@Ignore
public class UserTest extends AbstractTest {

    private ObjectFactory<User> userFactory;

    @Before
    public void initFactory() {
        userFactory = new ObjectFactory<>(User.class);
    }

    @After
    public void cleanUp() {
        //userFactory.cleanUp();
        userFactory = null;
    }

    @Test
    public void byFacebookIdNoUser() throws Throwable {
        //Optional<User> userOptional = JPA.withTransaction(() -> User.byFacebookId("NoUserWithThisFbId"));
        //assertThat(userOptional).isEqualTo(Optional.empty());
    }

    @Test
    public void byFacebookId() throws Throwable {
        String facebookId = "UserFacebookId";
        userFactory.create(ImmutableMap.of("facebookId", facebookId));
        //Optional<User> userOptional = JPA.withTransaction(() -> User.byFacebookId(facebookId));
        //assertThat(userOptional.isPresent()).isTrue();
        //assertThat(userOptional.get().facebookId).isEqualTo(facebookId);
    }

    @Test
    public void sendNotificationNoDevices() throws Throwable {

    }

    @Test
    public void sendSingleAndroidNotification() throws Throwable {
        User user = userFactory.create(ImmutableMap.of("devices", new IncludeEntity<>(Device.class, 1, TestUtils.mapOf("platform", Platform.android))));

        AbstractNotification mockNotification = mock(AbstractNotification.class);
        user.sendNotification(mockNotification);

        verify(mockNotification).send(Collections.singletonList(user.devices.get(0).regId), Collections.emptyList());
    }

    @Test
    public void sendSingleIosNotification() throws Throwable {
        User user = userFactory.create(ImmutableMap.of("devices", new IncludeEntity<>(Device.class, 1, TestUtils.mapOf("platform", Platform.ios))));

        AbstractNotification mockNotification = mock(AbstractNotification.class);
        user.sendNotification(mockNotification);

        verify(mockNotification).send(Collections.emptyList(), Collections.singletonList(user.devices.get(0).regId));
    }

    @Test
    public void sendNotificationBoth() throws Throwable {
        User user = userFactory.create();

        ObjectFactory<Device> deviceFactory = new ObjectFactory<>(Device.class);
        List<Device> androidDevices = deviceFactory.createList(3, ImmutableMap.of("platform", Platform.android, "user", user));
        List<Device> iosDevices = deviceFactory.createList(2, ImmutableMap.of("platform", Platform.ios, "user", user));
        List<Device> allDevices = new ArrayList<>();
        allDevices.addAll(androidDevices);
        allDevices.addAll(iosDevices);

        user.devices = allDevices;

        AbstractNotification mockNotification = mock(AbstractNotification.class);
        user.sendNotification(mockNotification);

        List<String> androidRegIds = androidDevices.stream().map(device -> device.regId).collect(Collectors.toList());
        List<String> iosRegIds = iosDevices.stream().map(device -> device.regId).collect(Collectors.toList());

        verify(mockNotification).send(androidRegIds, iosRegIds);

        deviceFactory.cleanUp();
    }

    @Test
    public void isAnon() throws Throwable {
        assertThat(userFactory.create().isAnon()).isFalse();
    }

    @Test
    public void getActual() throws Throwable {
        User user = userFactory.create();
        assertThat(user.getActual()).isEqualTo(user);
    }

    @Test
    public void sendChatRequestToNonAnonUserNoPrivateRoom() throws Throwable {
        JPA.withTransaction(() -> {
            User sender = userFactory.create();
            User receiver = userFactory.create();
//        User receiverMock = mock(User.class);
//        when(receiverMock.getActual()).thenReturn(receiverMock);
//        long receiverId = userFactory.create().userId;
//        when(receiverMock.userId).thenReturn(receiverId);
            //JPA.withTransaction(() -> sender.sendChatRequest(receiver));

            //assertThat(Request.getRequest(sender.userId, receiver.userId).isPresent()).isTrue();
            //verify(receiverMock).sendNotification(new ChatRequestNotification(sender));
        });
    }
}
