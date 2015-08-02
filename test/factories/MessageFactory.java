package factories;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableMap;
import daos.impl.MessageDaoImpl;
import models.Message;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class MessageFactory extends GenericFactory<Message> {

    public enum Trait implements ObjectMutator<Message> {
        WITH_SENDER {
            @Override
            public void apply(Message msg) throws IllegalAccessException, InstantiationException {
                msg.sender = new UserFactory().create();
            }
        },
        WITH_ANON_SENDER {
            @Override
            public void apply(Message msg) throws IllegalAccessException, InstantiationException {
                msg.sender = new AnonUserFactory().create();
            }
        },
        WITH_PUBLIC_ROOM {
            @Override
            public void apply(Message msg) throws InstantiationException, IllegalAccessException {
                msg.room = new PublicRoomFactory().create();
            }
        },
        WITH_PRIVATE_ROOM {
            @Override
            public void apply(Message msg) throws InstantiationException, IllegalAccessException {
                msg.room = new PrivateRoomFactory().create();
            }
        },
        PERSISTED {
            @Override
            public void apply(Message message) throws IllegalAccessException, InstantiationException {
                new MessageDaoImpl().save(message);
            }
        }
    }

    public MessageFactory() {
        super(Message.class);
    }

    @Override
    Map<String, Object> getDefaultProperties() {
        Faker faker = new Faker();

        return new ImmutableMap.Builder<String, Object>()
                .put("message", faker.lorem().sentence())
                .build();
    }


}
