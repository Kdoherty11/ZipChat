package factories;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class MessageDefaults extends FactoryDefaults {
    @Override
    public Map<String, Object> getDefaults() {
        return new ImmutableMap.Builder<String, Object>()
                .put("message", faker.lorem().sentence())
                .build();
    }
}
