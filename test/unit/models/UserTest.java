package unit.models;

import models.entities.AbstractUser;
import models.entities.User;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

/**
 * Created by kdoherty on 7/29/15.
 */
public class UserTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(AbstractUser.class)
                .withRedefinedSubclass(User.class)
                .verify();
    }

}
