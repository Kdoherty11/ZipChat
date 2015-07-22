package unit.services;

import daos.DeviceDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.DeviceService;
import services.impl.DeviceServiceImpl;

import static org.junit.Assert.assertNotNull;

/**
 * Created by kdoherty on 7/8/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceServiceTest {

    private DeviceService deviceService;

    @Mock
    private DeviceDao deviceDao;

    @Before
    public void setUp() {
        deviceService = new DeviceServiceImpl(deviceDao);
    }

    @Test
    public void testConstructor() {
        assertNotNull(deviceService);
    }
}
