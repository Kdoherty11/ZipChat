package utils;

import com.google.common.collect.ImmutableMap;
import play.Application;
import play.test.WithApplication;

import static play.test.Helpers.fakeApplication;

/**
 * Created by kdoherty on 7/31/15.
 */
public class WithDatabaseApplication extends WithApplication {

    @Override
    protected Application provideApplication() {
        return fakeApplication(ImmutableMap.of(
                "db.default.driver", "org.h2.Driver",
                "db.default.url", "jdbc:h2:mem:play-test-jpa",
                "db.default.jndiName", "DefaultDS",
                "jpa.default", "testPersistenceUnit"
        ));
    }
}
