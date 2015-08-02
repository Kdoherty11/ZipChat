package unit.models;

import integration.AbstractTest;
import models.Device;
import models.User;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by kevin on 6/22/15.
 */
@Ignore
public class DeviceTest extends AbstractTest {

    @Test(expected = NullPointerException.class)
    public void constructorNullUser() {
        new Device(null, "regId", Device.Platform.android);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullRegId() {
        new Device(new User(), null, Device.Platform.android);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullPlatform() {
        new Device(new User(), "regId", null);
    }
}
