package unit.models;

import models.entities.AbstractRoom;
import models.entities.PrivateRoom;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

/**
 * Created by kdoherty on 7/30/15.
 */
public class PrivateRoomTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(AbstractRoom.class)
                .withRedefinedSubclass(PrivateRoom.class)
                .verify();
    }

}
