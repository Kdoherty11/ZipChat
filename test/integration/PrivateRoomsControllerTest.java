package integration;

public class PrivateRoomsControllerTest extends AbstractTest {

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
//            assertThat(adapter.getRoomCountByUserId(senderId)).isEqualTo(1);
//            assertThat(adapter.getRoomCountByUserId(receiverId)).isEqualTo(1);
//            assertThat(adapter.getRoomCountByUserId(findUnusedId(senderId, receiverId))).isZero();
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
//            assertThat(status(leaveRoomSuccess)).isEqualTo(OK);
//            assertThat(contentAsString(leaveRoomSuccess)).isEqualTo(BaseController.OK_STRING);
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
//        assertThat(status(badRoomIdResult)).isEqualTo(NOT_FOUND);
//        assertThat(contentAsString(badRoomIdResult)).isEqualTo(TestUtils.withQuotes(DbUtils.buildEntityNotFoundString(PrivateRoom.class, badId)));
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
//        assertThat(status(userNotInRoomResult)).isEqualTo(BAD_REQUEST);
//        assertThat(contentAsString(userNotInRoomResult)).isEqualTo(
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

}
