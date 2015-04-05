package integration;

import adapters.PrivateRoomsControllerAdapter;
import adapters.RequestsControllerAdapter;
import adapters.UsersControllerAdapter;
import models.entities.PrivateRoom;
import models.entities.Request;
import org.json.JSONException;
import org.junit.Test;
import play.db.jpa.JPA;
import play.mvc.Result;
import utils.DbUtils;
import utils.TestUtils;

import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.status;

public class PrivateRoomsControllerTest extends AbstractControllerTest {

    private static final PrivateRoomsControllerAdapter adapter = PrivateRoomsControllerAdapter.INSTANCE;
    private static final UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;
    private static final RequestsControllerAdapter requestAdapter = RequestsControllerAdapter.INSTANCE;

    @Test
    public void testGetPrivateRoomsByUserId() throws JSONException {
        long createdResultId = requestAdapter.getCreateRequestId();
        requestAdapter.handleResponse(createdResultId, Request.Status.accepted.toString());

        JPA.withTransaction(() -> {
            Optional<Request> requestOptional = DbUtils.findEntityById(Request.class, createdResultId);
            Request request = requestOptional.get();
            final long senderId = request.sender.userId;
            final long receiverId = request.receiver.userId;

            assertThat(adapter.getRoomCountByUserId(senderId)).isEqualTo(1);
            assertThat(adapter.getRoomCountByUserId(receiverId)).isEqualTo(1);
            assertThat(adapter.getRoomCountByUserId(findUnusedId(senderId, receiverId))).isZero();
        });
    }

    @Test
    public void testLeaveRoomSuccess() throws JSONException {

        adapter.makePrivateRoom(privateRoom -> {
            long senderId = privateRoom.sender.userId;
            Result senderLeavingResult = adapter.leaveRoom(privateRoom.roomId, senderId);
//              assertThat(status(senderLeavingResult)).isEqualTo(OK);
//              assertThat(contentAsString(senderLeavingResult)).isEqualTo(
//              TestUtils.withQuotes(BaseController.OK_STRING));

        });

    }

    @Test
    public void testLeaveRoomBadRoomId() {
        long badId = 10;
        Result badRoomIdResult = adapter.leaveRoom(badId, 1);
        assertThat(status(badRoomIdResult)).isEqualTo(NOT_FOUND);
        assertThat(contentAsString(badRoomIdResult)).isEqualTo(TestUtils.withQuotes(DbUtils.buildEntityNotFoundString(PrivateRoom.ENTITY_NAME, badId)));
    }

    @Test
    public void testLeaveRoomUserNotInRoom() throws JSONException {
        adapter.makePrivateRoom(privateRoom -> {
            long senderId = privateRoom.sender.userId;
            long receiverId = privateRoom.receiver.userId;

            long otherId = findUnusedId(senderId, receiverId);
            Result userNotInRoomResult = adapter.leaveRoom(privateRoom.roomId, otherId);
            assertThat(status(userNotInRoomResult)).isEqualTo(BAD_REQUEST);
            assertThat(contentAsString(userNotInRoomResult)).isEqualTo(
                    TestUtils.withQuotes("Unable to remove user with ID " + otherId + " from the room because they are not in it"));
        });
    }

    public static long findUnusedId(long senderId, long receiverId) {
        for (long i = 0; i < 3; i++) {
            if (i != senderId && i != receiverId) {
                return i;
            }
        }
        throw new AssertionError();
    }

}
