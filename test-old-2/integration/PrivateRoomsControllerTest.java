package integration;

import controllers.MessagesController;
import controllers.PrivateRoomsController;
import factories.ObjectFactory;
import models.entities.PrivateRoom;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Result;
import play.test.WithApplication;
import services.impl.SecurityServiceImpl;
import services.PrivateRoomService;
import utils.JsonArrayIterator;
import utils.JsonValidator;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class PrivateRoomsControllerTest extends WithApplication {

    private PrivateRoomsController controller;
    private ObjectFactory<PrivateRoom> privateRoomFactory;

    @Mock
    private PrivateRoomService privateRoomService;

    @Mock
    private MessagesController messagesController;

    @Mock
    private SecurityServiceImpl securityServiceImpl;

    @Before
    public void setUp() throws Exception {
        privateRoomFactory = new ObjectFactory<>(PrivateRoom.class);
        controller = new PrivateRoomsController(privateRoomService, messagesController,
                securityServiceImpl);

        final GlobalSettings global = new GlobalSettings() {

            @Override
            public <T> T getControllerInstance(Class<T> clazz) {
                if (clazz.getSuperclass() == Action.class) {
                    return null;
                }

                return (T) controller;
            }

        };

        start(fakeApplication(global));
    }

    @Test
    public void getRoomsByUserIdEmpty() throws JSONException {
        List<PrivateRoom> rooms = new ArrayList<>();
        when(privateRoomService.findByUserId(1l)).thenReturn(rooms);

        Result result = route(fakeRequest(GET, "/privateRooms?userId=1"));
        assertEquals(status(result)).isEqualTo(OK);

        JSONArray resultRooms = new JSONArray(contentAsString(result));
        assertEquals(resultRooms.length()).isZero();
    }

    @Test
    public void getRoomsByUserId() throws Throwable {
        List<PrivateRoom> rooms = privateRoomFactory.createList(3, false);

        when(privateRoomService.findByUserId(1l)).thenReturn(rooms);

        Result result = route(fakeRequest(GET, "/privateRooms?userId=1"));
        assertEquals(status(result)).isEqualTo(OK);

        JSONArray resultRooms = new JSONArray(contentAsString(result));
        assertEquals(resultRooms.length()).isEqualTo(3);
        new JsonArrayIterator(resultRooms).forEach(JsonValidator::validatePrivateRoom);
    }

}


//public class PrivateRoomsControllerTest extends AbstractTest {

//    private static final PrivateRoomsControllerAdapter adapter = PrivateRoomsControllerAdapter.INSTANCE;
//    private static final UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;
//    private static final RequestsControllerAdapter requestAdapter = RequestsControllerAdapter.INSTANCE;

//    @Test
//    public void testGetPrivateRoomsByUserId() throws JSONException {
//        long createdResultId = requestAdapter.getCreateRequestId();
//        requestAdapter.handleResponse(createdResultId, Request.Status.accepted.toString());
//
//        JPA.withTransaction(() -> {
//            Optional<Request> requestOptional = DbUtils.findEntityById(Request.class, createdResultId);
//            Request request = requestOptional.get();
//            final long senderId = request.sender.userId;
//            final long receiverId = request.receiver.userId;
//
//            assertEquals(adapter.getRoomCountByUserId(senderId)).isEqualTo(1);
//            assertEquals(adapter.getRoomCountByUserId(receiverId)).isEqualTo(1);
//            assertEquals(adapter.getRoomCountByUserId(findUnusedId(senderId, receiverId))).isZero();
//        });
//    }
//
//    @Test
//    public void testLeaveRoomSuccess() throws Throwable {
//        JPA.withTransaction(() -> {
//            PrivateRoom room = adapter.makePrivateRoom();
//            Logger.debug("HERE: " + room);
//
//            Result leaveRoomSuccess = adapter.leaveRoom(room.roomId, room.sender.userId);
//            assertEquals(status(leaveRoomSuccess)).isEqualTo(OK);
//            assertEquals(contentAsString(leaveRoomSuccess)).isEqualTo(BaseController.OK_STRING);
//            Logger.debug("PASSED!!!!");
//        });
//
//
//    }
//
//    @Test
//    public void testLeaveRoomBadRoomId() {
//        long badId = 10;
//        Result badRoomIdResult = adapter.leaveRoom(badId, 1);
//        assertEquals(status(badRoomIdResult)).isEqualTo(NOT_FOUND);
//        assertEquals(contentAsString(badRoomIdResult)).isEqualTo(TestUtils.withQuotes(DbUtils.buildEntityNotFoundString(PrivateRoom.class, badId)));
//    }
//
//    @Test
//    public void testLeaveRoomUserNotInRoom() throws Throwable {
//        PrivateRoom privateRoom = adapter.makePrivateRoom();
//        long senderId = privateRoom.sender.userId;
//        long receiverId = privateRoom.receiver.userId;
//
//        long otherId = findUnusedId(senderId, receiverId);
//        Result userNotInRoomResult = adapter.leaveRoom(privateRoom.roomId, otherId);
//        assertEquals(status(userNotInRoomResult)).isEqualTo(BAD_REQUEST);
//        assertEquals(contentAsString(userNotInRoomResult)).isEqualTo(
//                TestUtils.withQuotes("Unable to remove user with ID " + otherId + " from the room because they are not in it"));
//    }
//
//    public static long findUnusedId(long senderId, long receiverId) {
//        for (long i = 0; i < 3; i++) {
//            if (i != senderId && i != receiverId) {
//                return i;
//            }
//        }
//        throw new AssertionError();
//    }

//}
