package unit.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import security.Secured;
import services.SecurityService;
import utils.TestUtils;
import utils.WithProductionApplication;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by kdoherty on 7/30/15.
 */
@RunWith(Enclosed.class)
public class SecuredTest {

    private static final String AUTH_TOKEN_HEADER;

    static {
        try {
            AUTH_TOKEN_HEADER = (String) TestUtils.getPrivateStaticField(Secured.class, "AUTH_TOKEN_HEADER");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static class SecuredNonProductionTests extends WithApplication {

        private Secured secured;

        @Mock
        private SecurityService securityService;

        private Helpers helpers;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            secured = new Secured(securityService);
            helpers = new Helpers();
        }

        @Test
        public void getUsernameNullHeaders() {
            Http.RequestBuilder builder = new Http.RequestBuilder();
            String username = helpers.invokeWithContext(builder, () -> secured.getUsername(Http.Context.current()));
            assertNotNull(username);
        }

        @Test
        public void getUsernameEmptyHeaders() {
            Http.RequestBuilder builder = new Http.RequestBuilder();
            builder.headers().put(AUTH_TOKEN_HEADER, new String[]{});
            String username = helpers.invokeWithContext(builder, () -> secured.getUsername(Http.Context.current()));
            assertNotNull(username);
        }

        @Test
        public void getUsernameFirstHeaderIsNull() {
            Http.RequestBuilder builder = new Http.RequestBuilder();
            builder.headers().put(AUTH_TOKEN_HEADER, new String[]{null});
            String username = helpers.invokeWithContext(builder, () -> secured.getUsername(Http.Context.current()));
            assertNotNull(username);
        }

        @Test
        public void getUsernameServiceReturnsEmptyUserId() {
            String authToken = "authToken";
            Http.RequestBuilder builder = new Http.RequestBuilder();
            builder.headers().put(AUTH_TOKEN_HEADER, new String[]{authToken});
            when(securityService.getUserId(authToken)).thenReturn(Optional.empty());
            String username = helpers.invokeWithContext(builder, () -> secured.getUsername(Http.Context.current()));
            assertNotNull(username);
        }
    }

    public static class SecuredProductionTests extends WithProductionApplication {

        private Secured secured;

        @Mock
        private SecurityService securityService;

        private Helpers helpers;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            secured = new Secured(securityService);
            helpers = new Helpers();
        }

        @Test
        public void getUsernameNullHeaders() {
            Http.RequestBuilder builder = new Http.RequestBuilder();
            String username = helpers.invokeWithContext(builder, () -> secured.getUsername(Http.Context.current()));
            assertNull(username);
        }

        @Test
        public void getUsernameEmptyHeaders() {
            Http.RequestBuilder builder = new Http.RequestBuilder();
            builder.headers().put(AUTH_TOKEN_HEADER, new String[]{});
            String username = helpers.invokeWithContext(builder, () -> secured.getUsername(Http.Context.current()));
            assertNull(username);
        }

        @Test
        public void getUsernameFirstHeaderIsNull() {
            Http.RequestBuilder builder = new Http.RequestBuilder();
            builder.headers().put(AUTH_TOKEN_HEADER, new String[]{null});
            String username = helpers.invokeWithContext(builder, () -> secured.getUsername(Http.Context.current()));
            assertNull(username);
        }

        @Test
        public void getUsernameServiceReturnsEmptyUserId() {
            String authToken = "authToken";
            Http.RequestBuilder builder = new Http.RequestBuilder();
            builder.headers().put(AUTH_TOKEN_HEADER, new String[]{authToken});
            when(securityService.getUserId(authToken)).thenReturn(Optional.empty());
            String username = helpers.invokeWithContext(builder, () -> secured.getUsername(Http.Context.current()));
            assertNull(username);
        }

        @Test
        public void getUserNameStoresUserIdInContextArgs() {
            String authToken = "authToken";
            long userId = 1;
            Http.RequestBuilder builder = new Http.RequestBuilder();
            builder.headers().put(AUTH_TOKEN_HEADER, new String[]{authToken});
            when(securityService.getUserId(authToken)).thenReturn(Optional.of(userId));
            helpers.invokeWithContext(builder, () -> {
                secured.getUsername(Http.Context.current());
                long argUserId = (long) Http.Context.current().args.get(Secured.USER_ID_KEY);
                assertEquals(userId, argUserId);
                return null;
            });
        }

        @Test
        public void getUsernameReturnsParsedUserId() {
            String authToken = "authToken";
            long userId = 1;
            Http.RequestBuilder builder = new Http.RequestBuilder();
            builder.headers().put(AUTH_TOKEN_HEADER, new String[]{authToken});
            when(securityService.getUserId(authToken)).thenReturn(Optional.of(userId));
            String username = helpers.invokeWithContext(builder, () -> secured.getUsername(Http.Context.current()));
            assertEquals(Long.toString(userId), username);
        }

        @Test
        public void onUnauthorizedCallsThroughToSuper() {
            Http.RequestBuilder builder = new Http.RequestBuilder();
            Result result = helpers.invokeWithContext(builder, () -> secured.onUnauthorized(Http.Context.current()));
            assertEquals(Helpers.UNAUTHORIZED, result.status());
        }

    }
}
