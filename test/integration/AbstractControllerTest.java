package integration;

import org.junit.After;
import org.junit.Before;
import play.test.FakeApplication;

import static play.test.Helpers.*;

public abstract class AbstractControllerTest {

    private FakeApplication application;

    @Before
    public void startApp() {
        application = fakeApplication(inMemoryDatabase());
        start(application);
    }

    @After
    public void stopApp() {
        stop(application);
    }
}
