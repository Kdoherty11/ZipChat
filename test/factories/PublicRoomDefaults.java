package factories;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class PublicRoomDefaults extends FactoryDefaults {
    @Override
    public Map<String, Object> getDefaults() {
        return new ImmutableMap.Builder<String, Object>()
                .put("name", faker.lorem().fixedString(20))
                .put("latitude", 0.0)
                .put("longitude", 10.0)
                .put("radius", 100)
                .build();
    }
}
