package unit.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.BaseController;
import controllers.RoomSocketsController;
import models.PrivateRoom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.PrivateRoomService;
import services.RoomSocketService;
import services.SecurityService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.test.Helpers.*;

/**
 * Created by kdoherty on 7/29/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class RoomSocketsControllerTest {

    private RoomSocketsController controller;

    @Mock
    private RoomSocketService roomSocketService;

    @Mock
    private PrivateRoomService privateRoomService;

    @Mock
    private SecurityService securityService;

    @Mock
    private WebSocket.In<JsonNode> in;

    @Mock
    private WebSocket.Out<JsonNode> out;

    @Before
    public void setUp() {
        controller = new RoomSocketsController(roomSocketService, privateRoomService, securityService);

        start(fakeApplication());
    }

    @Test
    public void joinPublicRoomIsSecured() {
        long roomId = 1;
        long userId = 2;
        String authToken = "myAuthToken";
        when(securityService.isUnauthorized(authToken, userId)).thenReturn(true);

        WebSocket<JsonNode> webSocket = controller.joinPublicRoom(roomId, userId, authToken);
        Result result = webSocket.rejectWith();

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void joinPublicRoomJoinsUsingServiceWhenSocketIsReady() throws Exception {
        long roomId = 1;
        long userId = 2;
        String authToken = "myAuthToken";
        when(securityService.isUnauthorized(authToken, userId)).thenReturn(false);
        doNothing().when(roomSocketService).join(roomId, userId, in, out);

        WebSocket<JsonNode> webSocket = controller.joinPublicRoom(roomId, userId, authToken);
        webSocket.onReady(in, out);

        verify(roomSocketService).join(roomId, userId, in, out);
    }

    @Test
    public void joinPublicRoomCatchesServiceException() throws Exception {
        long roomId = 1;
        long userId = 2;
        String authToken = "myAuthToken";
        when(securityService.isUnauthorized(authToken, userId)).thenReturn(false);
        doThrow(RuntimeException.class).when(roomSocketService).join(roomId, userId, in, out);

        WebSocket<JsonNode> webSocket = controller.joinPublicRoom(roomId, userId, authToken);
        webSocket.onReady(in, out);

        // Exception was caught
    }

    @Test
    public void joinPrivateRoomIsSecuredByUserId() throws Throwable {
        long roomId = 1;
        long userId = 2;
        String authToken = "myAuthToken";
        when(securityService.isUnauthorized(authToken, userId)).thenReturn(true);

        WebSocket<JsonNode> webSocket = controller.joinPrivateRoom(roomId, userId, authToken);
        Result result = webSocket.rejectWith();

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void joinPrivateRoomNotFound() throws Throwable {
        long roomId = 1;
        long userId = 2;
        String authToken = "myAuthToken";
        when(securityService.isUnauthorized(authToken, userId)).thenReturn(false);
        when(privateRoomService.findById(roomId)).thenReturn(Optional.empty());

        WebSocket<JsonNode> webSocket = controller.joinPrivateRoom(roomId, userId, authToken);
        Result result = webSocket.rejectWith();

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(PrivateRoom.class, roomId)));
    }

    @Test
    public void joinPrivateRoomIsSecuredToOnlySenderAndReceiver() throws Throwable {
        long roomId = 1;
        long userId = 2;
        String authToken = "myAuthToken";
        when(securityService.isUnauthorized(authToken, userId)).thenReturn(false);
        PrivateRoom room = mock(PrivateRoom.class);
        when(privateRoomService.findById(roomId)).thenReturn(Optional.of(room));
        when(securityService.isUnauthorized(authToken, room)).thenReturn(true);

        WebSocket<JsonNode> webSocket = controller.joinPrivateRoom(roomId, userId, authToken);
        Result result = webSocket.rejectWith();

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void joinPrivateRoomJoinsUsingServiceWhenSocketIsReady() throws Throwable {
        long roomId = 1;
        long userId = 2;
        String authToken = "myAuthToken";
        PrivateRoom room = mock(PrivateRoom.class);

        when(securityService.isUnauthorized(authToken, userId)).thenReturn(false);
        when(privateRoomService.findById(roomId)).thenReturn(Optional.of(room));
        when(securityService.isUnauthorized(authToken, room)).thenReturn(false);
        doNothing().when(roomSocketService).join(roomId, userId, in, out);

        WebSocket<JsonNode> webSocket = controller.joinPrivateRoom(roomId, userId, authToken);
        webSocket.onReady(in, out);

        verify(roomSocketService).join(roomId, userId, in, out);
    }

    @Test
    public void joinPrivateRoomCatchesServiceException() throws Throwable {
        long roomId = 1;
        long userId = 2;
        String authToken = "myAuthToken";
        PrivateRoom room = mock(PrivateRoom.class);

        when(securityService.isUnauthorized(authToken, userId)).thenReturn(false);
        when(privateRoomService.findById(roomId)).thenReturn(Optional.of(room));
        when(securityService.isUnauthorized(authToken, room)).thenReturn(false);
        doThrow(RuntimeException.class).when(roomSocketService).join(roomId, userId, in, out);

        WebSocket<JsonNode> webSocket = controller.joinPrivateRoom(roomId, userId, authToken);
        webSocket.onReady(in, out);

        // Exception was caught
    }

}
