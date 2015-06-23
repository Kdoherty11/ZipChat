package factories;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class UserDefaults extends FactoryDefaults {

    @Override
    public Map<String, Object> getDefaults() {
        return new ImmutableMap.Builder<String, Object>()
                .put("facebookId", faker.lorem().fixedString(17))
                .put("gender", faker.options().option(new String[]{"male", "female"}))
                .put("name", faker.name().fullName())
                .build();
    }
}
