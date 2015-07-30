package factories;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableMap;
import models.entities.AnonUser;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class AnonUserFactory extends GenericFactory<AnonUser> {

    public enum Trait implements ObjectMutator<AnonUser>{
        WITH_ACTUAL {
            @Override
            public void apply(AnonUser anonUser) throws InstantiationException, IllegalAccessException {
                anonUser.actual = new UserFactory().create();
            }
        },
        WITH_ROOM {
            @Override
            public void apply(AnonUser anonUser) throws InstantiationException, IllegalAccessException {
                anonUser.room = new PublicRoomFactory().create();
            }
        },
        WITH_ACTUAL_AND_ROOM {
            @Override
            public void apply(AnonUser anonUser) throws IllegalAccessException, InstantiationException {
                WITH_ACTUAL.apply(anonUser);
                WITH_ROOM.apply(anonUser);
            }
        }
    }

    public AnonUserFactory() {
        super(AnonUser.class);
    }

    @Override
    Map<String, Object> getDefaultProperties() {
        Faker faker = new Faker();

        return ImmutableMap.of("name", faker.name().fullName());
    }

}
