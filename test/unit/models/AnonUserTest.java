package unit.models;

import factories.AnonUserFactory;
import models.entities.AnonUser;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by kdoherty on 7/30/15.
 */
public class AnonUserTest {

    @Test
    public void equalsContract() {
        class LeafNodeAnonUser extends AnonUser {
            @Override
            public boolean canEqual(Object other) {
                return false;
            }
        }
        EqualsVerifier.forClass(AnonUser.class)
                .withRedefinedSuperclass()
                .withRedefinedSubclass(LeafNodeAnonUser.class)
                .verify();
    }

    @Test
    public void toStringAllNull() {
        AnonUser anonUser = new AnonUser();
        String actual = anonUser.toString();
        assertNotNull(actual);
    }

    @Test
    public void toStringNonNull() throws InstantiationException, IllegalAccessException {
        AnonUser anonUser = new AnonUserFactory().create(AnonUserFactory.Trait.WITH_ACTUAL_AND_ROOM);
        String actual = anonUser.toString();
        assertNotNull(actual);
    }

}
