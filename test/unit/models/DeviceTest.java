package unit.models;

import factories.DeviceFactory;
import models.Device;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by kdoherty on 7/29/15.
 */
public class DeviceTest {

    @Test
    public void setDeviceId() {
        long deviceId = 1;
        Device device = new Device();
        device.deviceId = deviceId;
        assertEquals(deviceId, device.deviceId);
    }

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Device.class)
                .suppress(Warning.STRICT_INHERITANCE) // Making equals/hashcode final messes up Mockito
                .verify();
    }

    @Test
    public void toStringAllNull() {
        Device device = new Device();
        String actual = device.toString();
        assertNotNull(actual);
    }

    @Test
    public void toStringNonNullAndroid() throws InstantiationException, IllegalAccessException {
        Device device = new DeviceFactory().create(DeviceFactory.Trait.ANDROID);
        String actual = device.toString();
        assertNotNull(actual);
    }

    @Test
    public void toStringNonNullIos() throws InstantiationException, IllegalAccessException {
        Device device = new DeviceFactory().create(DeviceFactory.Trait.IOS);
        String actual = device.toString();
        assertNotNull(actual);
    }
}
