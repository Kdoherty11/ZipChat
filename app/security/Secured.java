package security;

import com.google.inject.Inject;
import play.Play;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import services.SecurityService;

import java.util.Optional;

public class Secured extends Security.Authenticator {

    private static final String AUTH_TOKEN_HEADER = "X-Auth-Token";
    public static final String USER_ID_KEY = "userId";

    @Inject
    private final SecurityService securityService;

    @SuppressWarnings("unused") // Used in annotations
    public Secured() {
        this.securityService = Play.application().injector().instanceOf(SecurityService.class);
    }

    public Secured(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public String getUsername(Http.Context ctx) {
        String[] authTokenHeaderValues = ctx.request().headers().get(AUTH_TOKEN_HEADER);
        if (authTokenHeaderValues != null && authTokenHeaderValues.length == 1
                && authTokenHeaderValues[0] != null) {
            String jwt = authTokenHeaderValues[0];
            Optional<Long> userIdOptional = securityService.getUserId(jwt);

            if (userIdOptional.isPresent()) {
                long userId = userIdOptional.get();
                ctx.args.put(USER_ID_KEY, userId);
                return Long.toString(userId);
            }
        }

        if (!Play.isProd()) {
            return "-1";
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return super.onUnauthorized(ctx);
    }
}