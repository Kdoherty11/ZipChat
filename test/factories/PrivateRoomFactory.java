package factories;

import models.entities.PrivateRoom;

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
        }
    }

    public PrivateRoomFactory() {
        super(PrivateRoom.class);
    }

}
