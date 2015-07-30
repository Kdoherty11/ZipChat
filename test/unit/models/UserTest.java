package unit.models;

import factories.UserFactory;
import models.entities.AbstractUser;
import models.entities.User;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

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

    @Test
    public void toStringAllNull() {
        User user = new User();
        String actual = user.toString();
        assertNotNull(actual);
    }

    @Test
    public void toStringNonNull() throws InstantiationException, IllegalAccessException {
        User user = new UserFactory().create();
        String actual = user.toString();
        assertNotNull(actual);
    }

}
