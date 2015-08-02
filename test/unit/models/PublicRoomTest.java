package unit.models;

import factories.PublicRoomFactory;
import models.PublicRoom;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by kdoherty on 7/30/15.
 */
public class PublicRoomTest {

    @Test
    public void setRoomId() {
        long roomId = 1;
        PublicRoom room = new PublicRoom();
        room.roomId = roomId;
        assertEquals(roomId, room.roomId);
    }

    @Test
    public void equalsContract() {
        class LeafNodeRoom extends PublicRoom {
            @Override
            public boolean canEqual(Object other) {
                return false;
            }
        }
        EqualsVerifier.forClass(PublicRoom.class)
                .withRedefinedSuperclass()
                .withRedefinedSubclass(LeafNodeRoom.class)
                .verify();
    }

    @Test
    public void toStringAllNull() {
        PublicRoom publicRoom = new PublicRoom();
        String actual = publicRoom.toString();
        assertNotNull(actual);
    }

    @Test
    public void toStringNonNull() throws InstantiationException, IllegalAccessException {
        PublicRoom publicRoom = new PublicRoomFactory().create();
        String actual = publicRoom.toString();
        assertNotNull(actual);
    }

}
