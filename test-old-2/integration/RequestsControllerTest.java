package integration;

import org.junit.Ignore;

@Ignore
public class RequestsControllerTest extends AbstractTest {

//    RequestsControllerAdapter adapter = RequestsControllerAdapter.INSTANCE;
//
//    @Test
//    public void createRequestSuccess() throws JSONException {
//        UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;
//        Result senderCreateResult = userAdapter.createUser();
//        JSONObject senderCreateJson = new JSONObject(contentAsString(senderCreateResult));
//
//        Result receiverCreateResult = userAdapter.createUser();
//        JSONObject receiverCreateJson = new JSONObject(contentAsString(receiverCreateResult));
//
//        long senderId = senderCreateJson.getLong(UsersControllerAdapter.ID_KEY);
//        long receiverId = receiverCreateJson.getLong(UsersControllerAdapter.ID_KEY);
//
//        Result createResult = adapter.createRequest(senderId, receiverId, MESSAGE);
//        assertEquals(status(createResult)).isEqualTo(CREATED);
//
//        JSONObject createJson = new JSONObject(contentAsString(createResult));
//        assertEquals(createJson.getLong(ID_KEY)).isPositive();
//        assertEquals(createJson.getJSONObject(SENDER_KEY).toString()).isEqualTo(senderCreateJson.toString());
//        assertEquals(createJson.getJSONObject(RECEIVER_KEY).toString()).isEqualTo(receiverCreateJson.toString());
//        assertEquals(createJson.getString(MESSAGE_KEY)).isEqualTo(MESSAGE);
//        assertEquals(createJson.getString(STATUS_KEY)).isEqualTo(Request.Status.pending.toString());
//        long timeStamp = createJson.getLong(TIMESTAMP_KEY);
//        assertEquals(timeStamp).isPositive();
//        assertEquals(createJson.getLong(RESPONDED_TIMESTAMP_KEY)).isZero();
//
//        Result getRequestByReceiverResult = adapter.getRequestsByReceiver(receiverId);
//        assertEquals(status(getRequestByReceiverResult)).isEqualTo(OK);
//
//        JSONArray getJson = new JSONArray(contentAsString(getRequestByReceiverResult));
//        assertEquals(getJson.length()).isEqualTo(1);
//
//        JSONObject requestJson = getJson.getJSONObject(0);
//        assertEquals(requestJson.getLong(ID_KEY)).isPositive();
//        assertEquals(requestJson.getJSONObject(SENDER_KEY).toString()).isEqualTo(senderCreateJson.toString());
//        assertEquals(requestJson.getJSONObject(RECEIVER_KEY).toString()).isEqualTo(receiverCreateJson.toString());
//        assertEquals(requestJson.getString(MESSAGE_KEY)).isEqualTo(MESSAGE);
//        assertEquals(requestJson.getString(STATUS_KEY)).isEqualTo(STATUS);
//        assertEquals(requestJson.getLong(TIMESTAMP_KEY)).isEqualTo(timeStamp);
//        assertEquals(requestJson.getLong(RESPONDED_TIMESTAMP_KEY)).isZero();
//    }
//
//    @Test
//    public void createDuplicateRequest() throws JSONException {
//        UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;
//        long senderId = userAdapter.getCreateUserId();
//        long receiverId = userAdapter.getCreateUserId();
//
//        adapter.createRequest(senderId, receiverId, null);
//
//        Result duplicateResult = adapter.createRequest(senderId, receiverId, null);
//        assertEquals(status(duplicateResult)).isEqualTo(BAD_REQUEST);
//
//        String expectedMessage = "A request with sender " + senderId + " and receiver " + receiverId + " already exists";
//        assertEquals(contentAsString(duplicateResult)).isEqualTo("{\"\":[\"" + expectedMessage + "\"]}");
//    }
//
//    @Test
//    public void createRequestNoSender() throws JSONException {
//        Result noSenderResult = adapter.createRequest(null, SENDER_KEY);
//        assertEquals(status(noSenderResult)).isEqualTo(BAD_REQUEST);
//        assertEquals(contentAsString(noSenderResult)).isEqualTo(TestUtils.withQuotes("Request.sender is required!"));
//    }
//
//    @Test
//    public void createRequestNoReceiver() throws JSONException {
//        Result noReceiverResult = adapter.createRequest(null, RECEIVER_KEY);
//        assertEquals(status(noReceiverResult)).isEqualTo(BAD_REQUEST);
//        assertEquals(contentAsString(noReceiverResult)).isEqualTo(TestUtils.withQuotes("Request.receiver is required!"));
//    }
//
//    @Test
//    public void createRequestBadSender() throws JSONException {
//        long senderId = 500;
//        Result badSenderResult = adapter.createRequest(senderId, null, MESSAGE);
//        assertEquals(status(badSenderResult)).isEqualTo(NOT_FOUND);
//        assertEquals(contentAsString(badSenderResult)).isEqualTo(TestUtils.withQuotes(DbUtils.buildEntityNotFoundString(User.class, senderId)));
//    }
//
//    @Test
//    public void createRequestBadReceiver() throws JSONException {
//        long receiverId = 500;
//        Result badReceiverResult = adapter.createRequest(null, receiverId, MESSAGE);
//        assertEquals(status(badReceiverResult)).isEqualTo(NOT_FOUND);
//        assertEquals(contentAsString(badReceiverResult)).isEqualTo(TestUtils.withQuotes(DbUtils.buildEntityNotFoundString(User.class, receiverId)));
//    }
//
//    @Test
//    public void createRequestNegativeId() throws JSONException {
//        Result negativeIdResult = adapter.createRequest(null, -1L, MESSAGE);
//        assertEquals(status(negativeIdResult)).isEqualTo(BAD_REQUEST);
//        assertEquals(contentAsString(negativeIdResult)).isEqualTo(TestUtils.withQuotes("receiver must be a positive long"));
//
//    }
//
//    @Test
//    public void createRequestNotNumericId() throws JSONException {
//        Map<String, String> formData = new HashMap<>();
//        formData.put(SENDER_KEY, "NotALong");
//        Result notNumericIdResult = adapter.createRequest(formData);
//        assertEquals(status(notNumericIdResult)).isEqualTo(BAD_REQUEST);
//        assertEquals(contentAsString(notNumericIdResult)).isEqualTo(TestUtils.withQuotes("sender must be a positive long"));
//    }
//
//    @Test
//    public void getRequestsByReceiver() throws JSONException {
//        Result createResult = adapter.createRequest();
//        JSONObject receiverJson = new JSONObject(contentAsString(createResult)).getJSONObject(RECEIVER_KEY);
//        long receiverId = receiverJson.getLong(UsersControllerAdapter.ID_KEY);
//
//        Result requestsByReceiverId = adapter.getRequestsByReceiver(receiverId);
//        assertEquals(status(requestsByReceiverId)).isEqualTo(OK);
//
//        JSONArray requestsByReceiverJson = new JSONArray(contentAsString(requestsByReceiverId));
//        assertEquals(requestsByReceiverJson.length()).isEqualTo(1);
//    }
//
//    @Test
//    public void respondToRequestAcceptedSuccess() throws JSONException {
//        long createdResultId = adapter.getCreateRequestId();
//
//        Result handleResponseResult = adapter.handleResponse(createdResultId, Request.Status.accepted.toString());
//        assertEquals(status(handleResponseResult)).isEqualTo(OK);
//        assertEquals(contentAsString(handleResponseResult)).isEqualTo("\"" + BaseController.OK_STRING + "\"");
//
//        JPA.withTransaction(() -> {
//            Optional<Request> requestOptional = DbUtils.findEntityById(Request.class, createdResultId);
//            assertEquals(requestOptional.isPresent()).isTrue();
//
//            Request request = requestOptional.get();
//            assertEquals(request.status).isEqualTo(Request.Status.accepted);
//            assertEquals(request.respondedTimeStamp).isPositive();
//
//            long senderId = request.sender.userId;
//            long receiverId = request.receiver.userId;
//
//            String queryString = "select p from PrivateRoom p where (p.sender.userId = :senderId and p.senderInRoom = true) and (p.receiver.userId = :receiverId and p.receiverInRoom = true)";
//
//            TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
//                    .setParameter("senderId", senderId)
//                    .setParameter("receiverId", receiverId);
//
//            List<PrivateRoom> privateRoomList = query.getResultList();
//            assertEquals(privateRoomList).hasSize(1);
//
//            PrivateRoom privateRoom = privateRoomList.get(0);
//
//            assertEquals(privateRoom.roomId).isPositive();
//            assertEquals(privateRoom.messages).isEmpty();
//            assertEquals(privateRoom.request.equals(request));
//            assertEquals(privateRoom.sender).isEqualTo(request.sender);
//            assertEquals(privateRoom.receiver).isEqualTo(request.receiver);
//            assertEquals(privateRoom.timeStamp).isPositive();
//            assertEquals(privateRoom.lastActivity).isEqualTo(privateRoom.timeStamp);
//        });
//    }
//
//    @Test
//    public void respondToRequestDeniedSuccess() throws JSONException {
//        long createdResultId = adapter.getCreateRequestId();
//
//        Result handleResponseResult = adapter.handleResponse(createdResultId, Request.Status.denied.toString());
//        assertEquals(status(handleResponseResult)).isEqualTo(OK);
//        assertEquals(contentAsString(handleResponseResult)).isEqualTo("\"" + BaseController.OK_STRING + "\"");
//
//        JPA.withTransaction(() -> {
//            Optional<Request> requestOptional = DbUtils.findEntityById(Request.class, createdResultId);
//            assertEquals(requestOptional.isPresent()).isTrue();
//
//            Request request = requestOptional.get();
//            assertEquals(request.status).isEqualTo(Request.Status.denied);
//            assertEquals(request.respondedTimeStamp).isPositive();
//
//            long senderId = request.sender.userId;
//            long receiverId = request.receiver.userId;
//
//            String queryString = "select p from PrivateRoom p where (p.sender.userId = :senderId and p.senderInRoom = true) and (p.receiver.userId = :receiverId and p.receiverInRoom = true)";
//
//            TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
//                    .setParameter("senderId", senderId)
//                    .setParameter("receiverId", receiverId);
//
//            List<PrivateRoom> privateRoomList = query.getResultList();
//            assertEquals(privateRoomList).isEmpty();
//        });
//    }
//
//    @Test
//    public void handleResponseBadRequestId() {
//        int badId = 1;
//        Result badIdResult = adapter.handleResponse(badId, Request.Status.accepted.toString());
//        assertEquals(status(badIdResult)).isEqualTo(NOT_FOUND);
//        assertEquals(contentAsString(badIdResult).contains(DbUtils.buildEntityNotFoundString(Request.class, badId)));
//    }
//
//    @Test
//    public void handleResponseNoStatus() throws JSONException {
//        long createdRequestId = adapter.getCreateRequestId();
//
//        Result noStatusResult = adapter.handleResponse(createdRequestId, null);
//        assertEquals(status(noStatusResult)).isEqualTo(BAD_REQUEST);
//        assertEquals(contentAsString(noStatusResult)).contains("This field is required");
//    }
//
//    @Test
//    public void handleResponsePending() throws JSONException {
//        long createdRequestId = adapter.getCreateRequestId();
//
//        Result noStatusResult = adapter.handleResponse(createdRequestId, Request.Status.pending.toString());
//        assertEquals(status(noStatusResult)).isEqualTo(BAD_REQUEST);
//        assertEquals(contentAsString(noStatusResult)).contains("Invalid value");
//    }
//
//    @Test
//    public void handleBadResponse() throws JSONException {
//        long createdRequestId = adapter.getCreateRequestId();
//
//        Result noStatusResult = adapter.handleResponse(createdRequestId, "mayonnaise");
//        assertEquals(status(noStatusResult)).isEqualTo(BAD_REQUEST);
//        assertEquals(contentAsString(noStatusResult)).contains("Invalid value");
//    }
//
//    @Test
//    public void getStatus() throws JSONException {
//
//        UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;
//        long senderId = userAdapter.getCreateUserId();
//        long receiverId = userAdapter.getCreateUserId();
//
//        adapter.createRequest(senderId, receiverId, null);
//
//        Result getStatusResult = adapter.getStatus(senderId, receiverId);
//        assertEquals(status(getStatusResult)).isEqualTo(OK);
//        assertEquals(contentAsString(getStatusResult)).isEqualTo(TestUtils.withQuotes(Request.Status.pending.toString()));
//    }
//
//    @Test
//    public void getStatusNone() {
//        Result noRequestResult = adapter.getStatus(1, 2);
//        assertEquals(status(noRequestResult)).isEqualTo(OK);
//        assertEquals(contentAsString(noRequestResult)).isEqualTo(TestUtils.withQuotes("none"));
//    }
//
//    @Test
//    public void testConstructor() {
//        RequestsController controller = new RequestsController();
//        assertEquals(controller).isNotNull();
//    }
}
