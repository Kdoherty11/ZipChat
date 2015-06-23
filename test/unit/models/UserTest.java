package unit.models;

import com.google.common.collect.ImmutableMap;
import factories.ObjectFactory;
import integration.AbstractTest;
import models.entities.User;
import org.junit.Ignore;
import org.junit.Test;
import play.db.jpa.JPA;

import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by kevin on 6/21/15.
 */
@Ignore
public class UserTest extends AbstractTest {

    @Test
    public void byFacebookIdNoUser() throws Throwable {
        Optional<User> userOptional = JPA.withTransaction(() -> User.byFacebookId("NoUserWithThisFbId"));
        assertThat(userOptional).isEqualTo(Optional.empty());
    }

    @Test
    public void byFacebookId() throws Throwable {
        String facebookId = "UserFacebookId";
        new ObjectFactory<>(User.class).create(ImmutableMap.of("facebookId", facebookId));
        Optional<User> userOptional = JPA.withTransaction(() -> User.byFacebookId(facebookId));
        assertThat(userOptional.isPresent()).isTrue();
        assertThat(userOptional.get().facebookId).isEqualTo(facebookId);
    }

    @Test
    public void isAnon() throws Throwable {
        assertThat(new ObjectFactory<>(User.class).create().isAnon()).isFalse();
    }

    @Test
    public void getActual() throws Throwable {
        User user = new ObjectFactory<>(User.class).create();
        assertThat(user.getActual()).isEqualTo(user);
    }
}
