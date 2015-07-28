package unit.controllers;

import controllers.UsersController;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.SecurityService;
import services.UserService;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.start;

/**
 * Created by kdoherty on 7/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class UsersControllerTest {

    private UsersController controller;

    @Mock
    private UserService userService;

    @Mock
    private SecurityService securityService;

    @Before
    public void setUp() {
        controller = new UsersController(userService, securityService);

        start(fakeApplication());
    }

}
