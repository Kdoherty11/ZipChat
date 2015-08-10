package services.impl;

import com.google.common.base.Strings;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import io.jsonwebtoken.*;
import models.PrivateRoom;
import play.Logger;
import play.Play;
import play.mvc.Http;
import security.Secured;
import services.PrivateRoomService;
import services.SecurityService;

import java.util.Date;
import java.util.Optional;

/**
 * Created by kevin on 6/3/15.
 */
public class SecurityServiceImpl implements SecurityService {

    private final PrivateRoomService privateRoomService;

    private static final String SIGNING_KEY = Play.application().configuration()
            .getString("jwt.signing.key", "default_signing_key");

    private static final String ISSUER = "ZipChat";

    // 1 day in millis
    private static final long EXPIRATION_TIME_MILLIS = 1000L * 60L * 60L * 24L;

    @Inject
    public SecurityServiceImpl(final PrivateRoomService privateRoomService) {
        this.privateRoomService = privateRoomService;
    }

    @Override
    public String generateAuthToken(long userId) {
        long issuedMillis = System.currentTimeMillis();
        long expMillis = issuedMillis + EXPIRATION_TIME_MILLIS;

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuer(ISSUER)
                .setIssuedAt(new Date(issuedMillis))
                .setExpiration(new Date(expMillis))
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY)
                .compact();
    }

    @Override
    public Optional<Long> getUserId(String jwt) {
        if (Strings.isNullOrEmpty(jwt)) {
            return Optional.empty();
        }
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(jwt).getBody();
        } catch (SignatureException signatureException) {
            Logger.error("JWT has a different signing key", signatureException);
            return Optional.empty();
        } catch (ExpiredJwtException expiredJwtException) {
            Logger.debug("JWT is expired");
            return Optional.empty();
        } catch (MalformedJwtException e) {
            Logger.warn("JWT is malformed", e);
            return Optional.empty();
        } catch (UnsupportedJwtException e) {
            Logger.debug("JWT is unsupported", e);
            return Optional.empty();
        }

        String issuer = claims.getIssuer();
        if (!issuer.equals(ISSUER)) {
            Logger.error("JWT has unknown issuer: " + issuer);
            return Optional.empty();
        }

        String userIdStr = claims.getSubject();
        Optional<Long> userIdOptional = Optional.ofNullable(Longs.tryParse(userIdStr));
        if (!userIdOptional.isPresent()) {
            throw new RuntimeException("JWT userId " + userIdStr + "can't be converted to a long");
        }

        return userIdOptional;
    }

    @Override
    public boolean isUnauthorized(long userId) {
        return userId != getTokenUserId() && Play.isProd();
    }

    @Override
    public boolean isUnauthorized(PrivateRoom privateRoom) {
        return !privateRoomService.isUserInRoom(privateRoom, getTokenUserId()) && Play.isProd();
    }

    @Override
    public boolean isUnauthorized(String authToken, long userId) {
        Optional<Long> userIdOptional = getUserId(authToken);
        return (!userIdOptional.isPresent() || userIdOptional.get() != userId)
                && Play.isProd();
    }

    @Override
    public boolean isUnauthorized(String authToken, PrivateRoom privateRoom) {
        Optional<Long> userIdOptional = getUserId(authToken);

        return (!userIdOptional.isPresent() || !privateRoomService.isUserInRoom(privateRoom, userIdOptional.get()))
                && Play.isProd();
    }

    @Override
    public long getTokenUserId() {
        return Play.isProd() ? (long) Http.Context.current().args.get(Secured.USER_ID_KEY) : 1;
    }
}
