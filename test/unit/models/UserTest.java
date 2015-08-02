package unit.models;

import factories.UserFactory;
import models.User;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by kdoherty on 7/29/15.
 */
public class UserTest {

    @Test
    public void equalsContract() {
        class LeafNodeUser extends User {
            @Override
            public boolean canEqual(Object other) {
                return false;
            }
        }
        EqualsVerifier.forClass(User.class)
                .withRedefinedSuperclass()
                .withRedefinedSubclass(LeafNodeUser.class)
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
