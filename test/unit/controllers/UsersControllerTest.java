package unit.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import controllers.UsersController;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import services.SecurityService;
import services.UserService;
import validation.validators.RequiredValidator;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

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

    private Helpers helpers;


    @Before
    public void setUp() {
        controller = new UsersController(userService, securityService);
        helpers = new Helpers();

        start(fakeApplication());
    }

    @Test
    public void createWithoutFbAccessToken() {
        Map<String, String> bodyForm = Collections.emptyMap();
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createUser);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains(RequiredValidator.ERROR_MESSAGE));
    }

    @Test
    public void createGetFacebookInfoReturnsError() {
        String fbAccessToken = "myFbAccessToken";
        JsonNode fbJsonResponse = Json.newObject()
                .set("error", Json.newObject().put("message", "errorMessage"));
        when(userService.getFacebookInformation(fbAccessToken)).thenReturn(fbJsonResponse);

        Map<String, String> bodyForm = ImmutableMap.of("fbAccessToken", fbAccessToken);
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createUser);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains(fbJsonResponse.asText()));
    }

    @Test
    public void createNewUserSavesUserWithReturnedFbInfo() {
        String fbAccessToken = "myFbAccessToken";
        long userId = 1;
        String fbId = "myFbId";
        String name = "My Name";
        String gender = "male";
        JsonNode fbJsonResponse = Json.newObject()
                .put("id", fbId)
                .put("name", name)
                .put("gender", gender);

        User createdUser = new User();
        createdUser.facebookId = fbId;
        createdUser.name = name;
        createdUser.gender = gender;

        when(userService.getFacebookInformation(fbAccessToken)).thenReturn(fbJsonResponse);
        when(userService.findByFacebookId(fbId)).thenReturn(Optional.empty());
        doAnswer(invocation -> ((User) invocation.getArguments()[0]).userId = userId).
                when(userService).save(eq(createdUser));
        when(securityService.generateAuthToken(userId)).thenReturn("myAuthToken");

        Map<String, String> bodyForm = ImmutableMap.of("fbAccessToken", fbAccessToken);
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        helpers.invokeWithContext(builder, controller::createUser);

        verify(userService).save(createdUser);
    }

    @Test
    public void createUserWithFbIdAlreadyExistsMergesUserWithNewFbInfo() {
        String fbAccessToken = "myFbAccessToken";
        String fbId = "myFbId";
        String name = "My Name";
        String gender = "male";
        JsonNode fbJsonResponse = Json.newObject()
                .put("id", fbId)
                .put("name", name)
                .put("gender", gender);
        when(userService.getFacebookInformation(fbAccessToken)).thenReturn(fbJsonResponse);

        long userId = 1;
        User existingUser = mock(User.class);
        when(existingUser.userId).thenReturn(userId);


        when(userService.findByFacebookId(fbId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userService).merge(any(User.class));
        when(securityService.generateAuthToken(userId)).thenReturn("myAuthToken");

        Map<String, String> bodyForm = ImmutableMap.of("fbAccessToken", fbAccessToken);
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        helpers.invokeWithContext(builder, controller::createUser);

        verify(userService).merge(argThat(new ArgumentMatcher<User>() {
            @Override
            public boolean matches(Object o) {
                User user = (User) o;
                return user.userId == userId
                        && user.facebookId.equals(fbId)
                        && user.name.equals(name)
                        && user.gender.endsWith(gender);
            }
        }));
    }

    @Test
    public void createResponseContainsUserIdFbIdNameAndAuthToken() throws IOException {
        String fbAccessToken = "myFbAccessToken";
        long userId = 1;
        String fbId = "myFbId";
        String name = "My Name";
        String gender = "male";
        JsonNode fbJsonResponse = Json.newObject()
                .put("id", fbId)
                .put("name", name)
                .put("gender", gender);
        when(userService.getFacebookInformation(fbAccessToken)).thenReturn(fbJsonResponse);

        when(userService.findByFacebookId(fbId)).thenReturn(Optional.empty());
        doAnswer(invocation -> ((User) invocation.getArguments()[0]).userId = userId).
                when(userService).save(any(User.class));
        String authToken = "myAuthToken";
        when(securityService.generateAuthToken(userId)).thenReturn(authToken);

        Map<String, String> bodyForm = ImmutableMap.of("fbAccessToken", fbAccessToken);
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createUser);

        JsonNode resultJson = new ObjectMapper().readTree(contentAsString(result));
        assertEquals(userId, resultJson.get("userId").asLong());
        assertEquals(fbId, resultJson.get("facebookId").asText());
        assertEquals(name, resultJson.get("name").asText());
        assertEquals(authToken, resultJson.get("authToken").asText());
    }

    @Test
    public void createReturnsOkIfUpdatedExistingUser() {
        String fbAccessToken = "myFbAccessToken";
        String fbId = "myFbId";
        String name = "My Name";
        String gender = "male";
        JsonNode fbJsonResponse = Json.newObject()
                .put("id", fbId)
                .put("name", name)
                .put("gender", gender);
        when(userService.getFacebookInformation(fbAccessToken)).thenReturn(fbJsonResponse);

        long userId = 1;
        User existingUser = mock(User.class);
        when(existingUser.userId).thenReturn(userId);

        when(userService.findByFacebookId(fbId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userService).merge(any(User.class));
        when(securityService.generateAuthToken(userId)).thenReturn("myAuthToken");

        Map<String, String> bodyForm = ImmutableMap.of("fbAccessToken", fbAccessToken);
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createUser);

        assertEquals(OK, result.status());
    }

    @Test
    public void createReturnsCreatedIfCreatedNewUser() {
        String fbAccessToken = "myFbAccessToken";
        long userId = 1;
        String fbId = "myFbId";
        String name = "My Name";
        String gender = "male";
        JsonNode fbJsonResponse = Json.newObject()
                .put("id", fbId)
                .put("name", name)
                .put("gender", gender);

        User createdUser = new User();
        createdUser.facebookId = fbId;
        createdUser.name = name;
        createdUser.gender = gender;

        when(userService.getFacebookInformation(fbAccessToken)).thenReturn(fbJsonResponse);
        when(userService.findByFacebookId(fbId)).thenReturn(Optional.empty());
        doAnswer(invocation -> ((User) invocation.getArguments()[0]).userId = userId).
                when(userService).save(eq(createdUser));
        when(securityService.generateAuthToken(userId)).thenReturn("myAuthToken");

        Map<String, String> bodyForm = ImmutableMap.of("fbAccessToken", fbAccessToken);
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createUser);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void authReturnsFacebookError() {
        String fbAccessToken = "myFbAccessToken";
        JsonNode fbJsonResponse = Json.newObject()
                .set("error", Json.newObject().put("message", "errorMessage"));
        when(userService.getFacebookInformation(fbAccessToken)).thenReturn(fbJsonResponse);

        Result result = controller.auth(fbAccessToken);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains(fbJsonResponse.asText()));
    }

    @Test
    public void authUserNotFoundByFacebookId() {
        String fbAccessToken = "myFbAccessToken";
        String fbId = "myFbId";
        JsonNode fbJsonResponse = Json.newObject().put("id", fbId);
        when(userService.getFacebookInformation(fbAccessToken)).thenReturn(fbJsonResponse);
        when(userService.findByFacebookId(fbId)).thenReturn(Optional.empty());

        Result result = controller.auth(fbAccessToken);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains("facebook access token doesn't match any users"));
    }

    @Test
    public void authReturnsAuthTokenAndOkResponse() throws IOException {
        String fbAccessToken = "myFbAccessToken";
        String fbId = "myFbId";
        long userId = 1;
        JsonNode fbJsonResponse = Json.newObject().put("id", fbId);
        when(userService.getFacebookInformation(fbAccessToken)).thenReturn(fbJsonResponse);
        User user = mock(User.class);
        when(user.userId).thenReturn(userId);
        when(userService.findByFacebookId(fbId)).thenReturn(Optional.of(user));

        String authToken = "myAuthToken";
        when(securityService.generateAuthToken(userId)).thenReturn(authToken);

        Result result = controller.auth(fbAccessToken);

        assertEquals(OK, result.status());
        JsonNode resultJson = new ObjectMapper().readTree(contentAsString(result));
        assertEquals(authToken, resultJson.get("authToken").asText());
    }

}
