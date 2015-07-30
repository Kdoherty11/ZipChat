package unit.models;

import models.entities.AbstractUser;
import models.entities.AnonUser;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

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

}
