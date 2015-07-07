package factories;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableMap;
import models.entities.User;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class UserFactory extends GenericFactory<User> {

    public UserFactory() {
        super(User.class);
    }

    @Override
    Map<String, Object> getDefaultProperties() {
        Faker faker = new Faker();
        return new ImmutableMap.Builder<String, Object>()
                .put("facebookId", faker.lorem().fixedString(17))
                .put("gender", faker.options().option(new String[]{"male", "female"}))
                .put("name", faker.name().fullName())
                .build();
    }
}
