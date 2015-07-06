package factories;

import com.google.common.collect.ImmutableMap;
import models.entities.AnonUser;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class AnonUserFactory extends GenericFactory<AnonUser> {

    public AnonUserFactory() {
        super(AnonUser.class);
    }

    @Override
    Map<String, Object> getDefaultProperties() {
        return ImmutableMap.of("name", faker.name().fullName());
    }

}
