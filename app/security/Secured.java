package security;

import play.Play;

import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import java.util.Optional;

public class Secured extends Security.Authenticator {

    private static final String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
    public static final String USER_ID_KEY = "userId";

    @Override
    public String getUsername(Http.Context ctx) {
        String[] authTokenHeaderValues = ctx.request().headers().get(AUTH_TOKEN_HEADER);
        if (authTokenHeaderValues != null && authTokenHeaderValues.length == 1
                && authTokenHeaderValues[0] != null) {
            String jwt = authTokenHeaderValues[0];
            Logger.debug("Received jwt: " + jwt);
            Optional<Long> userIdOptional = SecurityHelper.getUserId(jwt);

            if (userIdOptional.isPresent()) {
                long userId = userIdOptional.get();
                ctx.args.put(USER_ID_KEY, userId);
                Logger.debug("Received legit authToken from user " + userId);
                return Long.toString(userId);
            } else {
                Logger.debug("Could not parse user id from auth token");
            }
        }

        if (Play.isDev()) {
            return "-1";
        }

        return null;
    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return super.onUnauthorized(ctx);
    }
}