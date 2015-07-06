package factories;

import com.google.common.collect.ImmutableMap;
import models.Platform;
import models.entities.Device;

import java.util.Map;
import java.util.Random;

/**
 * Created by kevin on 6/23/15.
 */
public class DeviceFactory extends GenericFactory<Device> {

    public DeviceFactory() {
        super(Device.class);
    }

    @Override
    Map<String, Object> getDefaultProperties() {
        Platform[] platforms = Platform.class.getEnumConstants();
        return new ImmutableMap.Builder<String, Object>()
                .put("regId", faker.lorem().fixedString(20))
                .put("platform", platforms[new Random().nextInt(platforms.length)])
                .build();
    }
}
