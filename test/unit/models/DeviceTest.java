package unit.models;

import integration.AbstractTest;
import models.Platform;
import models.entities.Device;
import models.entities.User;
import org.junit.Test;

/**
 * Created by kevin on 6/22/15.
 */
public class DeviceTest extends AbstractTest {

    @Test(expected = NullPointerException.class)
    public void constructorNullUser() {
        new Device(null, "regId", Platform.android);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullRegId() {
        new Device(new User(), null, Platform.android);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullPlatform() {
        new Device(new User(), "regId", null);
    }
}
