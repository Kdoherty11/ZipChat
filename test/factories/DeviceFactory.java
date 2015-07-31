package factories;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableMap;
import daos.impl.DeviceDaoImpl;
import models.Platform;
import models.entities.Device;

import java.util.Map;
import java.util.Random;

/**
 * Created by kevin on 6/23/15.
 */
public class DeviceFactory extends GenericFactory<Device> {

    public enum Trait implements ObjectMutator<Device>{
        ANDROID {
            @Override
            public void apply(Device device) {
                device.platform = Platform.android;
            }
        },
        IOS {
            @Override
            public void apply(Device device) {
                device.platform = Platform.ios;
            }
        },
        PERSISTED {
            @Override
            public void apply(Device device) throws IllegalAccessException, InstantiationException {
                new DeviceDaoImpl().save(device);
            }
        }
    }

    public DeviceFactory() {
        super(Device.class);
    }

    @Override
    Map<String, Object> getDefaultProperties() {
        Faker faker = new Faker();

        Platform[] platforms = Platform.class.getEnumConstants();
        return new ImmutableMap.Builder<String, Object>()
                .put("regId", faker.lorem().fixedString(20))
                .put("platform", platforms[new Random().nextInt(platforms.length)])
                .build();
    }
}
