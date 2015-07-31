package factories;

import daos.impl.PrivateRoomDaoImpl;
import models.entities.PrivateRoom;
import models.entities.Request;

/**
 * Created by kdoherty on 7/6/15.
 */
public class PrivateRoomFactory extends GenericFactory<PrivateRoom> {

    public enum Trait implements ObjectMutator<PrivateRoom> {
        WITH_SENDER {
            @Override
            public void apply(PrivateRoom privateRoom) throws IllegalAccessException, InstantiationException {
                privateRoom.sender = new UserFactory().create();
            }
        },
        WITH_RECEIVER {
            @Override
            public void apply(PrivateRoom privateRoom) throws IllegalAccessException, InstantiationException {
                privateRoom.sender = new UserFactory().create();
            }
        },
        WITH_SENDER_AND_RECEIVER {
            @Override
            public void apply(PrivateRoom privateRoom) throws IllegalAccessException, InstantiationException {
                UserFactory userFactory = new UserFactory();
                privateRoom.sender = userFactory.create();
                privateRoom.receiver = userFactory.create();
            }
        },
        WITH_REQUEST {
            @Override
            public void apply(PrivateRoom privateRoom) throws IllegalAccessException, InstantiationException {
                Request request = new RequestFactory().create(RequestFactory.Trait.WITH_SENDER_AND_RECEIVER);
                setRequest(privateRoom, request);
            }
        },
        WITH_PERSISTED_REQUEST {
            @Override
            public void apply(PrivateRoom privateRoom) throws IllegalAccessException, InstantiationException {
                Request request = new RequestFactory().create(RequestFactory.Trait.PERSISTED_WITH_SENDER_AND_RECEIVER);
                setRequest(privateRoom, request);
            }
        },
        PERSISTED {
            @Override
            public void apply(PrivateRoom privateRoom) throws IllegalAccessException, InstantiationException {
                new PrivateRoomDaoImpl().save(privateRoom);
            }
        },
        PERSISTED_WITH_REQUEST {
            @Override
            public void apply(PrivateRoom privateRoom) throws IllegalAccessException, InstantiationException {
                Request request = new RequestFactory().create(RequestFactory.Trait.PERSISTED_WITH_SENDER_AND_RECEIVER);
                setRequest(privateRoom, request);
                PERSISTED.apply(privateRoom);
            }
        };

        public static void setRequest(PrivateRoom room, Request request) {
            room.request = request;
            room.sender = request.sender;
            room.receiver = request.receiver;
        }
    }

    public PrivateRoomFactory() {
        super(PrivateRoom.class);
    }

}
