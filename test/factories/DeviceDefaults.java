package factories;

import com.google.common.collect.ImmutableMap;
import models.Platform;

import java.util.Map;
import java.util.Random;

/**
 * Created by kevin on 6/23/15.
 */
public class DeviceDefaults extends FactoryDefaults {

    @Override
    public Map<String, Object> getDefaults() {
        Platform[] platforms = Platform.class.getEnumConstants();
        return new ImmutableMap.Builder<String, Object>()
                .put("regId", faker.lorem().fixedString(20))
                .put("platform", platforms[new Random().nextInt(platforms.length)])
                .build();
    }

}
