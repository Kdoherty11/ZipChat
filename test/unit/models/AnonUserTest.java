package unit.models;

import factories.AnonUserFactory;
import models.entities.AbstractUser;
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
        EqualsVerifier.forClass(AbstractUser.class)
                .withRedefinedSubclass(AnonUser.class)
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
