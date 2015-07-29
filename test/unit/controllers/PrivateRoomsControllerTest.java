package unit.controllers;

import com.google.gson.Gson;
import controllers.BaseController;
import controllers.PrivateRoomsController;
import factories.MessageFactory;
import models.entities.Message;
import models.entities.PrivateRoom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.mvc.Result;
import services.MessageService;
import services.PrivateRoomService;
import services.SecurityService;
import services.UserService;
import utils.DbUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static play.libs.Json.toJson;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.OK;
import static play.mvc.Results.ok;
import static play.test.Helpers.*;

/**
 * Created by kdoherty on 7/9/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class PrivateRoomsControllerTest {

    private PrivateRoomsController controller;
    private Gson gson;

    @Mock
    private PrivateRoomService privateRoomService;

    @Mock
    private MessageService messageService;

    @Mock
    private SecurityService securityService;

    @Mock
    private UserService userService;

    @Before
    public void setUp() {
        gson = new Gson();
        controller = new PrivateRoomsController(privateRoomService, messageService,
                securityService);

        start(fakeApplication());
    }

    @Test
    public void getRoomsByUserIdIsSecured() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(true);

        Result result = controller.getRoomsByUserId(userId);

        assertEquals(FORBIDDEN, result.status());
        verifyZeroInteractions(privateRoomService);
    }

    @Test
    public void getRoomsByUserId() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        List<PrivateRoom> rooms = new ArrayList<>();
        when(privateRoomService.findByUserId(userId)).thenReturn(rooms);

        Result result = controller.getRoomsByUserId(userId);

        assertEquals(OK, result.status());
        assertEquals(contentAsString(ok(toJson(rooms))), contentAsString(result));
    }

    @Test
    public void leaveRoomIsSecured() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(true);

        Result result = controller.leaveRoom(1, userId);

        assertEquals(FORBIDDEN, result.status());
        verifyZeroInteractions(privateRoomService);
    }

    @Test
    public void leaveRoomRoomNotFound() {
        long roomId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(privateRoomService.findById(roomId)).thenReturn(Optional.empty());

        Result result = controller.leaveRoom(roomId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(DbUtils.buildEntityNotFoundString(PrivateRoom.class, roomId)));
    }

    @Test
    public void leaveRoom() {
        long roomId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        PrivateRoom room = new PrivateRoom();
        when(privateRoomService.findById(roomId)).thenReturn(Optional.of(room));
        when(privateRoomService.removeUser(room, userId)).thenReturn(true);

        Result result = controller.leaveRoom(roomId, userId);

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(BaseController.OK_STRING));
    }

    @Test
    public void leaveRoomUserNotInRoom() {
        long roomId = 1;
        long userId = 2;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        PrivateRoom room = new PrivateRoom();
        when(privateRoomService.findById(roomId)).thenReturn(Optional.of(room));
        when(privateRoomService.removeUser(room, userId)).thenReturn(false);

        Result result = controller.leaveRoom(roomId, userId);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Unable to remove user"));
    }

    @Test
    public void getMessagesRoomNotFound() {
        long roomId = 1;
        when(privateRoomService.findById(roomId)).thenReturn(Optional.empty());

        Result result = controller.getMessages(roomId, 1, 1);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(DbUtils.buildEntityNotFoundString(PrivateRoom.class, roomId)));
    }

    @Test
    public void getMessagesIsSecured() {
        long roomId = 1;
        PrivateRoom room = new PrivateRoom();
        when(privateRoomService.findById(roomId)).thenReturn(Optional.of(room));
        when(securityService.isUnauthorized(room)).thenReturn(true);

        Result result = controller.getMessages(roomId, 1, 1);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void getMessagesWithNegativeOffset() {
        long roomId = 1;
        PrivateRoom mockRoom = mock(PrivateRoom.class);
        when(privateRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));

        final Result result = controller.getMessages(roomId, 1, -1);

        assertEquals(BAD_REQUEST, result.status());
        String resultString = contentAsString(result);
        assertTrue(resultString.contains("offset"));
        assertTrue(resultString.contains("must be at least 0"));
        verifyZeroInteractions(messageService);
    }

    @Test
    public void getMessagesWithNegativeLimit() {
        long roomId = 1;
        PrivateRoom mockRoom = mock(PrivateRoom.class);
        when(privateRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));

        final Result result = controller.getMessages(roomId, -1, 1);

        assertEquals(BAD_REQUEST, result.status());
        String resultString = contentAsString(result);
        assertTrue(resultString.contains("limit"));
        assertTrue(resultString.contains("must be at least 0"));
        verifyZeroInteractions(messageService);
    }

    @Test
    public void getMessagesWorksWithZeroLimitAndZeroOffset() throws IllegalAccessException, InstantiationException {
        long roomId = 1;
        int limit = 0;
        int offset = 0;
        PrivateRoom mockRoom = mock(PrivateRoom.class);
        when(privateRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        MessageFactory messageFactory = new MessageFactory();
        List<Message> messages = messageFactory.createList(3);
        when(messageService.getMessages(roomId, limit, offset)).thenReturn(messages);

        Result result = controller.getMessages(roomId, limit, offset);

        assertEquals(OK, result.status());
        Message[] returnedMessages = gson.fromJson(contentAsString(result), Message[].class);
        assertEquals(3, returnedMessages.length);
        for (int i = 0; i < returnedMessages.length; i++) {
            assertEquals(returnedMessages[i], messages.get(i));
        }
    }

}
