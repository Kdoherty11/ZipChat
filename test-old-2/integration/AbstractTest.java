package integration;

import models.entities.*;
import org.junit.After;
import org.junit.Before;
import play.test.FakeApplication;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public abstract class AbstractTest {

    private FakeApplication application;

    @Before
    public void startApp() {
        application = fakeApplication(inMemoryDatabase());
        start(application);
    }

    @After
    public void stopApp() {
        stop(application);
    }

    protected void testAbstractRoom(AbstractRoom room) {
        assertThat(room).isNotNull();
        assertThat(room.roomId).isPositive();
    }

    protected void testPersistedPublicRoom(PublicRoom room) {
        testAbstractRoom(room);
    }

    protected void testPersistedPrivateRoom(PrivateRoom room) {
        testAbstractRoom(room);
        testPersistedUser(room.sender);
        testPersistedUser(room.receiver);
    }

    protected void testPersistedRequest(Request request) {
        assertThat(request).isNotNull();
        assertThat(request.requestId).isPositive();
        testPersistedUser(request.sender);
        testPersistedUser(request.receiver);
    }

    protected void testPersistedUser(User user) {
        testAbstractUser(user);
    }

    protected void testAbstractUser(AbstractUser user) {
        assertThat(user).isNotNull();
        assertThat(user.userId).isPositive();
    }

    protected void testAnonUser(AnonUser anonUser) {
        testAbstractUser(anonUser);
        testPersistedPublicRoom(anonUser.room);
        testPersistedUser(anonUser.actual);
    }

    protected void testDevice(Device device) {
        assertThat(device).isNotNull();
        assertThat(device.deviceId).isPositive();
        testPersistedUser(device.user);
    }

    protected void testMessage(Message message) {
        assertThat(message).isNotNull();
        assertThat(message.messageId).isPositive();
        testAbstractRoom(message.room);
        testAbstractUser(message.sender);
    }
}
