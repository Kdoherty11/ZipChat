package unit.models;

import factories.PrivateRoomFactory;
import models.PrivateRoom;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by kdoherty on 7/30/15.
 */
public class PrivateRoomTest {

    @Test
    public void equalsContract() {
        class LeafNodeRoom extends PrivateRoom {
            @Override
            public boolean canEqual(Object other) {
                return false;
            }
        }
        EqualsVerifier.forClass(PrivateRoom.class)
                .withRedefinedSuperclass()
                .withRedefinedSubclass(LeafNodeRoom.class)
                .verify();
    }

    @Test
    public void toStringAllNull() {
        PrivateRoom privateRoom = new PrivateRoom();
        String actual = privateRoom.toString();
        assertNotNull(actual);
    }

    @Test
    public void toStringNonNull() throws InstantiationException, IllegalAccessException {
        PrivateRoom privateRoom = new PrivateRoomFactory().create(PrivateRoomFactory.Trait.WITH_SENDER_AND_RECEIVER);
        String actual = privateRoom.toString();
        assertNotNull(actual);
    }

}
