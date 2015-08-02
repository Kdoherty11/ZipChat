package integration;

import models.*;
import models.entities.*;
import org.junit.After;
import org.junit.Before;
import play.test.FakeApplication;

import static org.fest.assertions.Assertions.assertEquals;
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
        assertEquals(room).isNotNull();
        assertEquals(room.roomId).isPositive();
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
        assertEquals(request).isNotNull();
        assertEquals(request.requestId).isPositive();
        testPersistedUser(request.sender);
        testPersistedUser(request.receiver);
    }

    protected void testPersistedUser(User user) {
        testAbstractUser(user);
    }

    protected void testAbstractUser(AbstractUser user) {
        assertEquals(user).isNotNull();
        assertEquals(user.userId).isPositive();
    }

    protected void testAnonUser(AnonUser anonUser) {
        testAbstractUser(anonUser);
        testPersistedPublicRoom(anonUser.room);
        testPersistedUser(anonUser.actual);
    }

    protected void testDevice(Device device) {
        assertEquals(device).isNotNull();
        assertEquals(device.deviceId).isPositive();
        testPersistedUser(device.user);
    }

    protected void testMessage(Message message) {
        assertEquals(message).isNotNull();
        assertEquals(message.messageId).isPositive();
        testAbstractRoom(message.room);
        testAbstractUser(message.sender);
    }
}
