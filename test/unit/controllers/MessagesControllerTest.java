package unit.controllers;

import controllers.BaseController;
import controllers.MessagesController;
import models.Message;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.mvc.Result;
import services.MessageService;
import services.SecurityService;
import services.UserService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.test.Helpers.*;

/**
 * Created by kdoherty on 7/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagesControllerTest {

    private MessagesController controller;

    @Mock
    private MessageService messageService;

    @Mock
    private UserService userService;

    @Mock
    private SecurityService securityService;

    @Before
    public void setUp() {
        controller = new MessagesController(messageService, userService, securityService);

        start(fakeApplication());
    }

    @Test
    public void favoriteIsSecuredByFavoritorId() {
        long messageId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(true);

        Result result = controller.favorite(messageId, userId);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void favoriteMessageNotFound() {
        long messageId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.empty());

        Result result = controller.favorite(messageId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(Message.class, messageId)));
    }

    @Test
    public void favoriteFavoritorNotFound() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.empty());

        Result result = controller.favorite(messageId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(User.class, userId)));
    }

    @Test
    public void favoriteAlreadyFavoritedDoesNotCallServiceAndReturnsBadRequest() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(messageService.favorite(message, user)).thenReturn(false);

        Result result = controller.favorite(messageId, userId);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("has already favorited this message"));
    }

    @Test
    public void favoriteNotYetFavoritedCallsServiceAndReturnsOk() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(messageService.favorite(message, user)).thenReturn(true);

        Result result = controller.favorite(messageId, userId);

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(BaseController.OK_STRING));
    }

    @Test
    public void removeFavoriteIsSecuredByFavoritorId() {
        long messageId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(true);

        Result result = controller.removeFavorite(messageId, userId);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void removeFavoriteMessageNotFound() {
        long messageId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.empty());

        Result result = controller.removeFavorite(messageId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(Message.class, messageId)));
    }

    @Test
    public void removeFavoriteFavoritorNotFound() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.empty());

        Result result = controller.removeFavorite(messageId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(User.class, userId)));
    }

    @Test
    public void removeFavoriteNotFavoritedDoesNotCallServiceAndReturnsNotFound() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(messageService.removeFavorite(message, user)).thenReturn(false);

        Result result = controller.removeFavorite(messageId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains("has not favorited this message"));
    }

    @Test
    public void removeFavoriteCallsServiceAndReturnsOk() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(messageService.removeFavorite(message, user)).thenReturn(true);

        Result result = controller.removeFavorite(messageId, userId);

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(BaseController.OK_STRING));
    }

    @Test
    public void flagIsSecuredByUserId() {
        long messageId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(true);

        Result result = controller.flag(messageId, userId);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void flagMessageNotFound() {
        long messageId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.empty());

        Result result = controller.flag(messageId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(Message.class, messageId)));
    }

    @Test
    public void flagUserNotFound() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.empty());

        Result result = controller.flag(messageId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(User.class, userId)));
    }

    @Test
    public void flagAlreadyFlaggedDoesNotCallServiceAndReturnsBadRequest() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(messageService.flag(message, user)).thenReturn(false);

        Result result = controller.flag(messageId, userId);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("has already flagged this message"));
    }

    @Test
    public void flagNotYetFlaggedCallsServiceAndReturnsOk() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(messageService.flag(message, user)).thenReturn(true);

        Result result = controller.flag(messageId, userId);

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(BaseController.OK_STRING));
    }

    @Test
    public void removeFlagIsSecuredByUserId() {
        long messageId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(true);

        Result result = controller.removeFlag(messageId, userId);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void removeFlagMessageNotFound() {
        long messageId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.empty());

        Result result = controller.removeFlag(messageId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(Message.class, messageId)));
    }

    @Test
    public void removeFlagUserNotFound() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.empty());

        Result result = controller.removeFlag(messageId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(User.class, userId)));
    }

    @Test
    public void removeFlagNotFlaggedDoesNotCallServiceAndReturnsNotFound() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(messageService.removeFlag(message, user)).thenReturn(false);

        Result result = controller.removeFlag(messageId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains("has not flagged this message"));
    }

    @Test
    public void removeFlagCallsServiceAndReturnsOk() {
        long messageId = 1;
        long userId = 2;
        Message message = mock(Message.class);
        User user = mock(User.class);
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(messageService.removeFlag(message, user)).thenReturn(true);

        Result result = controller.removeFlag(messageId, userId);

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(BaseController.OK_STRING));
    }
}
