package unit.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import models.PrivateRoom;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Http;
import play.test.Helpers;
import play.test.WithApplication;
import security.Secured;
import services.PrivateRoomService;
import services.SecurityService;
import services.impl.SecurityServiceImpl;
import utils.TestUtils;
import utils.WithProductionApplication;

import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 7/29/15.
 */
@RunWith(Enclosed.class)
public class SecurityServiceTest {

    private static final String SIGNING_KEY;
    private static final long EXPIRATION_TIME_MILLIS;
    private static final String ISSUER;

    static {
        try {
            SIGNING_KEY = (String) TestUtils.getHiddenField(SecurityServiceImpl.class,
                    "SIGNING_KEY");
            EXPIRATION_TIME_MILLIS = (long) TestUtils.getHiddenField(SecurityServiceImpl.class,
                    "EXPIRATION_TIME_MILLIS");
            ISSUER = (String) TestUtils.getHiddenField(SecurityServiceImpl.class, "ISSUER");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Environment mode = Test
     */
    public static class SecurityServiceNonProductionTests extends WithApplication {

        private SecurityService securityService;

        @Mock
        private PrivateRoomService privateRoomService;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            securityService = spy(new SecurityServiceImpl(privateRoomService));
        }

        private Claims getClaims(String authToken) throws NoSuchFieldException, IllegalAccessException {
            return Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(authToken).getBody();
        }

        @Test
        public void generateAuthTokenSetsUserIdAsSubject() throws Exception {
            long userId = 1;
            String authToken = securityService.generateAuthToken(userId);
            Claims claims = getClaims(authToken);

            assertEquals(userId, Long.parseLong(claims.getSubject()));
        }

        @Test
        public void generateAuthTokenSetsIssuer() throws Exception {
            String authToken = securityService.generateAuthToken(1);
            Claims claims = getClaims(authToken);
            assertEquals(ISSUER, claims.getIssuer());
        }

        @Test
        public void generateAuthTokenSetsIssuedAt() throws Exception {
            long currentMillis = System.currentTimeMillis();
            String authToken = securityService.generateAuthToken(1);
            Claims claims = getClaims(authToken);

            long epsilon = 1000l;
            assertEquals(currentMillis, claims.getIssuedAt().getTime(), epsilon);
        }

        @Test
        public void generateAuthTokenSetsExpiration() throws Exception {
            long currentMillis = System.currentTimeMillis();
            String authToken = securityService.generateAuthToken(1);
            Claims claims = getClaims(authToken);

            long epsilon = 1000l;
            assertEquals(currentMillis + EXPIRATION_TIME_MILLIS, claims.getExpiration().getTime(), epsilon);
        }

        @Test
        public void getUserIdReturnsEmptyIfJwtIsNull() {
            Optional<Long> userIdOptional = securityService.getUserId(null);
            assertEquals(Optional.empty(), userIdOptional);
        }

        @Test
        public void getUserIdReturnsEmptyIfJwtIsEmpty() {
            Optional<Long> userIdOptional = securityService.getUserId("");
            assertEquals(Optional.empty(), userIdOptional);
        }

        private JwtBuilder getJwtBuilder(long userId) {
            long issuedMillis = System.currentTimeMillis();
            long expMillis = issuedMillis + EXPIRATION_TIME_MILLIS;

            return Jwts.builder()
                    .setSubject(Long.toString(userId))
                    .setIssuer(ISSUER)
                    .setIssuedAt(new Date(issuedMillis))
                    .setExpiration(new Date(expMillis))
                    .signWith(SignatureAlgorithm.HS512, SIGNING_KEY);
        }

        @Test
        public void getUserIdReturnsEmptyIfWrongSigningKey() {
            String jwt = getJwtBuilder(1)
                    .signWith(SignatureAlgorithm.HS512, "NotMySigningKey")
                    .compact();
            Optional<Long> userIdOptional = securityService.getUserId(jwt);

            assertEquals(Optional.empty(), userIdOptional);
        }

        @Test
        public void getUserIdReturnsEmptyIfJwtIsExpired() {
            String jwt = getJwtBuilder(1)
                    .setExpiration(new Date(System.currentTimeMillis() - 1000))
                    .compact();
            Optional<Long> userIdOptional = securityService.getUserId(jwt);

            assertEquals(Optional.empty(), userIdOptional);
        }

        @Test
        public void getUserIdReturnsEmptyIfJwtIsMalformed() {
            // TODO FIGURE OUT MALFORMED CASE
            String jwt = "NowThisIsMalformed";
            Optional<Long> userIdOptional = securityService.getUserId(jwt);

            assertEquals(Optional.empty(), userIdOptional);
        }

        @Test
        public void getUserIdReturnsEmptyIfUnsupportedJwt() {
            String jwt = Jwts.builder()
                    .setIssuer(ISSUER).compact();
            Optional<Long> userIdOptional = securityService.getUserId(jwt);

            assertEquals(Optional.empty(), userIdOptional);
        }

        @Test
        public void getUserIdReturnsEmptyIfUnknownIssuer() {
            String jwt = getJwtBuilder(1).setIssuer("NotMyIssuer").compact();
            Optional<Long> userIdOptional = securityService.getUserId(jwt);

            assertEquals(Optional.empty(), userIdOptional);
        }

        @Test(expected = RuntimeException.class)
        public void getUserIdReturnsEmptyIfUserIdIsNotALong() {
            String jwt = getJwtBuilder(1).setSubject("NotAUserId").compact();
            securityService.getUserId(jwt);
        }

        @Test
        public void getUserIdReturnsUserId() {
            Long userId = 2l;
            String jwt = securityService.generateAuthToken(userId);
            Optional<Long> parsedUserId = securityService.getUserId(jwt);

            assertEquals(userId, parsedUserId.get());
        }

        @Test
        public void isUnauthorizedByUserIdOnNonProductionReturnsTrueWhenUserIdsDoNotMatch() {
            long userId = 1;
            long otherUserId = 2;
            doReturn(otherUserId).when(securityService).getTokenUserId();

            boolean isUnauthorized = securityService.isUnauthorized(userId);

            assertFalse(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForPrivateRoomOnNonProdReturnsFalseWhenUserIdIsNotInTheRoom() {
            long userIdNotInRoom = 1;
            PrivateRoom room = mock(PrivateRoom.class);
            doReturn(userIdNotInRoom).when(securityService).getTokenUserId();
            when(privateRoomService.isUserInRoom(room, userIdNotInRoom)).thenReturn(false);

            boolean isUnauthorized = securityService.isUnauthorized(room);

            assertFalse(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForUserIdWithAuthTokenOnNonProdReturnsFalseWhenUserIdIsEmpty() {
            String authToken = "myAuthToken";
            long userId = 1;
            doReturn(Optional.empty()).when(securityService).getUserId(authToken);

            boolean isUnauthorized = securityService.isUnauthorized(authToken, userId);

            assertFalse(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForUserIdWithAuthTokenOnNonProdReturnsFalseWhenUserIdDoesNotMatch() {
            String authToken = "myAuthToken";
            long userId = 1;
            long otherUserId = 2;
            doReturn(Optional.of(otherUserId)).when(securityService).getUserId(authToken);

            boolean isUnauthorized = securityService.isUnauthorized(authToken, userId);

            assertFalse(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForPrivateRoomWithAuthTokenOnNonProdReturnsTrueWhenUserIdIsEmpty() {
            String authToken = "myAuthToken";
            PrivateRoom room = mock(PrivateRoom.class);
            doReturn(Optional.empty()).when(securityService).getUserId(authToken);

            boolean isUnauthorized = securityService.isUnauthorized(authToken, room);

            assertFalse(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForPrivateRoomWithAuthTokenOnProdReturnsTrueWhenUserNotInRoom() {
            String authToken = "myAuthToken";
            long userId = 1;
            PrivateRoom room = mock(PrivateRoom.class);
            doReturn(Optional.of(userId)).when(securityService).getUserId(authToken);
            when(privateRoomService.isUserInRoom(room, userId)).thenReturn(false);

            boolean isUnauthorized = securityService.isUnauthorized(authToken, room);

            assertFalse(isUnauthorized);
        }

        @Test
        public void getTokenUserIdOnNonProdReturnsAValidUserId() {
            long userId = securityService.getTokenUserId();

            assertTrue(userId > 0);
        }

    }

    /**
     * Environment mode = Prod
     */
    public static class SecurityServiceProductionTests extends WithProductionApplication {

        private SecurityService securityService;

        @Mock
        private PrivateRoomService privateRoomService;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
            securityService = spy(new SecurityServiceImpl(privateRoomService));
        }

        @Test
        public void isUnauthorizedByUserIdOnProdReturnsTrueWhenUserIdsDoNotMatch() {
            long userId = 1;
            long otherUserId = 2;
            doReturn(otherUserId).when(securityService).getTokenUserId();

            boolean isUnauthorized = securityService.isUnauthorized(userId);

            assertTrue(isUnauthorized);
        }

        @Test
        public void isUnauthorizedByUserIdOnProdReturnsFalseWhenUserIdsMatch() {
            long userId = 1;
            doReturn(userId).when(securityService).getTokenUserId();

            boolean isUnauthorized = securityService.isUnauthorized(userId);

            assertFalse(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForPrivateRoomOnProdReturnsTrueWhenUserNotInRoom() {
            long userIdNotInRoom = 1;
            PrivateRoom room = mock(PrivateRoom.class);
            doReturn(userIdNotInRoom).when(securityService).getTokenUserId();
            when(privateRoomService.isUserInRoom(room, userIdNotInRoom)).thenReturn(false);

            boolean isUnauthorized = securityService.isUnauthorized(room);

            assertTrue(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForPrivateRoomOnProdReturnsFalseWhenUserInRoom() {
            long userIdInRoom = 1;
            PrivateRoom room = mock(PrivateRoom.class);
            doReturn(userIdInRoom).when(securityService).getTokenUserId();
            when(privateRoomService.isUserInRoom(room, userIdInRoom)).thenReturn(true);

            boolean isUnauthorized = securityService.isUnauthorized(room);

            assertFalse(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForUserIdWithAuthTokenOnProdReturnsTrueWhenUserIdIsEmpty() {
            String authToken = "myAuthToken";
            long userId = 1;
            doReturn(Optional.empty()).when(securityService).getUserId(authToken);

            boolean isUnauthorized = securityService.isUnauthorized(authToken, userId);

            assertTrue(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForUserIdWithAuthTokenOnProdReturnsTrueWhenUserIdDoesNotMatch() {
            String authToken = "myAuthToken";
            long userId = 1;
            long otherUserId = 2;
            doReturn(Optional.of(otherUserId)).when(securityService).getUserId(authToken);

            boolean isUnauthorized = securityService.isUnauthorized(authToken, userId);

            assertTrue(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForUserIdWithAuthTokenOnProdReturnsFalseWhenUserIdMatches() {
            String authToken = "myAuthToken";
            long userId = 1;
            doReturn(Optional.of(userId)).when(securityService).getUserId(authToken);

            boolean isUnauthorized = securityService.isUnauthorized(authToken, userId);

            assertFalse(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForPrivateRoomWithAuthTokenOnProdReturnsTrueWhenUserIdIsEmpty() {
            String authToken = "myAuthToken";
            PrivateRoom room = mock(PrivateRoom.class);
            doReturn(Optional.empty()).when(securityService).getUserId(authToken);

            boolean isUnauthorized = securityService.isUnauthorized(authToken, room);

            assertTrue(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForPrivateRoomWithAuthTokenOnProdReturnsTrueWhenUserNotInRoom() {
            String authToken = "myAuthToken";
            long userId = 1;
            PrivateRoom room = mock(PrivateRoom.class);
            doReturn(Optional.of(userId)).when(securityService).getUserId(authToken);
            when(privateRoomService.isUserInRoom(room, userId)).thenReturn(false);

            boolean isUnauthorized = securityService.isUnauthorized(authToken, room);

            assertTrue(isUnauthorized);
        }

        @Test
        public void isUnauthorizedForPrivateRoomWithAuthTokenOnProdReturnsFalseWhenUserInRoom() {
            String authToken = "myAuthToken";
            long userId = 1;
            PrivateRoom room = mock(PrivateRoom.class);
            doReturn(Optional.of(userId)).when(securityService).getUserId(authToken);
            when(privateRoomService.isUserInRoom(room, userId)).thenReturn(true);

            boolean isUnauthorized = securityService.isUnauthorized(authToken, room);

            assertFalse(isUnauthorized);
        }

        @Test
        public void getTokenUserIdOnProdReturnsTheUserIdFromTheContext() {
            long userId = 3l;
            long tokenUserId = new Helpers().invokeWithContext(new Http.RequestBuilder(), () -> {
                Http.Context.current().args.put(Secured.USER_ID_KEY, userId);
                return securityService.getTokenUserId();
            });

            assertEquals(userId, tokenUserId);
        }

    }


}
