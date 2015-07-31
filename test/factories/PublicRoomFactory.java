package factories;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableMap;
import daos.impl.PublicRoomDaoImpl;
import models.entities.PublicRoom;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class PublicRoomFactory extends GenericFactory<PublicRoom> {

    public enum Trait implements ObjectMutator<PublicRoom>{

        PERSISTED {
            @Override
            public void apply(PublicRoom publicRoom) throws IllegalAccessException, InstantiationException {
                new PublicRoomDaoImpl().save(publicRoom);
            }
        }
    }

    public PublicRoomFactory() {
        super(PublicRoom.class);
    }

    @Override
    Map<String, Object> getDefaultProperties() {
        Faker faker = new Faker();
        return new ImmutableMap.Builder<String, Object>()
                .put("name", faker.lorem().fixedString(20))
                .put("latitude", 0.0)
                .put("longitude", 10.0)
                .put("radius", 100)
                .build();
    }
}
