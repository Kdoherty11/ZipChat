package factories;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class AnonUserDefaults extends FactoryDefaults {

    @Override
    public Map<String, Object> getDefaults() {
        return ImmutableMap.of("name", faker.name().fullName());
    }

}
