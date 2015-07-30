package unit.models;

import factories.PublicRoomFactory;
import models.entities.AbstractRoom;
import models.entities.PublicRoom;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by kdoherty on 7/30/15.
 */
public class PublicRoomTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(AbstractRoom.class)
                .withRedefinedSubclass(PublicRoom.class)
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
