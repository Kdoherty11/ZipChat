package factories;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableMap;
import daos.impl.AnonUserDaoImpl;
import models.AnonUser;

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
        },
        PERSISTED {
            @Override
            public void apply(AnonUser anonUser) throws IllegalAccessException, InstantiationException {
                new AnonUserDaoImpl().save(anonUser);
            }
        },
        PERSISTED_WITH_ACTUAL_AND_ROOM {
            @Override
            public void apply(AnonUser anonUser) throws IllegalAccessException, InstantiationException {
                anonUser.room = new PublicRoomFactory().create(PublicRoomFactory.Trait.PERSISTED);
                anonUser.actual = new UserFactory().create(UserFactory.Trait.PERSISTED);
                PERSISTED.apply(anonUser);
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
