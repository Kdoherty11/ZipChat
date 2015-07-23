package unit.services;

import daos.AbstractUserDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.AbstractUserService;
import services.AnonUserService;
import services.UserService;
import services.impl.AbstractUserServiceImpl;

import static org.junit.Assert.assertNotNull;

/**
 * Created by kdoherty on 7/8/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractUserServiceTest {

    private AbstractUserService abstractUserService;

    @Mock
    private AbstractUserDao abstractUserDao;

    @Mock
    private UserService userService;

    @Mock
    private AnonUserService anonUserService;

    @Before
    public void setUp() {
        abstractUserService = new AbstractUserServiceImpl(abstractUserDao, userService, anonUserService);
    }

    @Test
    public void testConstructor() {
        assertNotNull(abstractUserService);
    }


}
