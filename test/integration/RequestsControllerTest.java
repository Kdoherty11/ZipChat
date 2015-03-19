package integration;

import adapters.RequestsControllerAdapter;
import adapters.UsersControllerAdapter;
import controllers.BaseController;
import controllers.RequestsController;
import models.entities.PrivateRoom;
import models.entities.Request;
import models.entities.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Result;
import utils.DbUtils;
import utils.TestUtils;

import javax.persistence.TypedQuery;
import java.util.*;

import static adapters.RequestsControllerAdapter.*;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.status;


public class RequestsControllerTest extends AbstractControllerTest {

    RequestsControllerAdapter adapter = RequestsControllerAdapter.INSTANCE;

    @Test
    public void createRequestSuccess() throws JSONException {
        UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;
        Result senderCreateResult = userAdapter.createUser();
        JSONObject senderCreateJson = new JSONObject(contentAsString(senderCreateResult));

        Result receiverCreateResult = userAdapter.createUser();
        JSONObject receiverCreateJson = new JSONObject(contentAsString(receiverCreateResult));

        long senderId = senderCreateJson.getLong(UsersControllerAdapter.ID_KEY);
        long receiverId = receiverCreateJson.getLong(UsersControllerAdapter.ID_KEY);

        Result createResult = adapter.createRequest(senderId, receiverId, MESSAGE);
        assertThat(status(createResult)).isEqualTo(CREATED);

        JSONObject createJson = new JSONObject(contentAsString(createResult));
        assertThat(createJson.getLong(ID_KEY)).isPositive();
        assertThat(createJson.getJSONObject(SENDER_KEY).toString()).isEqualTo(senderCreateJson.toString());
        assertThat(createJson.getJSONObject(RECEIVER_KEY).toString()).isEqualTo(receiverCreateJson.toString());
        assertThat(createJson.getString(MESSAGE_KEY)).isEqualTo(MESSAGE);
        assertThat(createJson.getString(STATUS_KEY)).isEqualTo(Request.Status.pending.toString());
        long timeStamp = createJson.getLong(TIMESTAMP_KEY);
        assertThat(timeStamp).isPositive();
        assertThat(createJson.getLong(RESPONDED_TIMESTAMP_KEY)).isZero();

        Result getRequestByReceiverResult = adapter.getRequestsByReceiver(receiverId);
        assertThat(status(getRequestByReceiverResult)).isEqualTo(OK);

        JSONArray getJson = new JSONArray(contentAsString(getRequestByReceiverResult));
        assertThat(getJson.length()).isEqualTo(1);

        JSONObject requestJson = getJson.getJSONObject(0);
        assertThat(requestJson.getLong(ID_KEY)).isPositive();
        assertThat(requestJson.getJSONObject(SENDER_KEY).toString()).isEqualTo(senderCreateJson.toString());
        assertThat(requestJson.getJSONObject(RECEIVER_KEY).toString()).isEqualTo(receiverCreateJson.toString());
        assertThat(requestJson.getString(MESSAGE_KEY)).isEqualTo(MESSAGE);
        assertThat(requestJson.getString(STATUS_KEY)).isEqualTo(STATUS);
        assertThat(requestJson.getLong(TIMESTAMP_KEY)).isEqualTo(timeStamp);
        assertThat(requestJson.getLong(RESPONDED_TIMESTAMP_KEY)).isZero();
    }

    @Test
    public void createDuplicateRequest() throws JSONException {
        UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;
        long senderId = userAdapter.getCreateUserId();
        long receiverId = userAdapter.getCreateUserId();

        adapter.createRequest(senderId, receiverId, null);

        Result duplicateResult = adapter.createRequest(senderId, receiverId, null);
        assertThat(status(duplicateResult)).isEqualTo(BAD_REQUEST);

        String expectedMessage = "A request with sender " + senderId + " and receiver " + receiverId + " already exists";
        assertThat(contentAsString(duplicateResult)).isEqualTo("{\"\":[\"" + expectedMessage + "\"]}");
    }

    @Test
    public void createRequestNoSender() throws JSONException {
        Result noSenderResult = adapter.createRequest(null, SENDER_KEY);
        assertThat(status(noSenderResult)).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(noSenderResult)).isEqualTo(TestUtils.withQuotes("Request.sender is required!"));
    }

    @Test
    public void createRequestNoReceiver() throws JSONException {
        Result noReceiverResult = adapter.createRequest(null, RECEIVER_KEY);
        assertThat(status(noReceiverResult)).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(noReceiverResult)).isEqualTo(TestUtils.withQuotes("Request.receiver is required!"));
    }

    @Test
    public void createRequestBadSender() throws JSONException {
        long senderId = 500;
        Result badSenderResult = adapter.createRequest(senderId, null, MESSAGE);
        assertThat(status(badSenderResult)).isEqualTo(NOT_FOUND);
        assertThat(contentAsString(badSenderResult)).isEqualTo(TestUtils.withQuotes(DbUtils.buildEntityNotFoundString(User.ENTITY_NAME, senderId)));
    }

    @Test
    public void createRequestBadReceiver() throws JSONException {
        long receiverId = 500;
        Result badReceiverResult = adapter.createRequest(null, receiverId, MESSAGE);
        assertThat(status(badReceiverResult)).isEqualTo(NOT_FOUND);
        assertThat(contentAsString(badReceiverResult)).isEqualTo(TestUtils.withQuotes(DbUtils.buildEntityNotFoundString(User.ENTITY_NAME, receiverId)));
    }

    @Test
    public void createRequestNegativeId() throws JSONException {
        Result negativeIdResult = adapter.createRequest(null, -1L, MESSAGE);
        assertThat(status(negativeIdResult)).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(negativeIdResult)).isEqualTo(TestUtils.withQuotes("receiver must be a positive long"));

    }

    @Test
    public void createRequestNotNumericId() throws JSONException {
        Map<String, String> formData = new HashMap<>();
        formData.put(SENDER_KEY, "NotALong");
        Result notNumericIdResult = adapter.createRequest(formData);
        assertThat(status(notNumericIdResult)).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(notNumericIdResult)).isEqualTo(TestUtils.withQuotes("sender must be a positive long"));
    }

    @Test
    public void getRequestsByReceiver() throws JSONException {
        Result createResult = adapter.createRequest();
        JSONObject receiverJson = new JSONObject(contentAsString(createResult)).getJSONObject(RECEIVER_KEY);
        long receiverId = receiverJson.getLong(UsersControllerAdapter.ID_KEY);

        Result requestsByReceiverId = adapter.getRequestsByReceiver(receiverId);
        assertThat(status(requestsByReceiverId)).isEqualTo(OK);

        JSONArray requestsByReceiverJson = new JSONArray(contentAsString(requestsByReceiverId));
        assertThat(requestsByReceiverJson.length()).isEqualTo(1);
    }

    @Test
    public void respondToRequestAcceptedSuccess() throws JSONException {
        long createdResultId = adapter.getCreateRequestId();

        Result handleResponseResult = adapter.handleResponse(createdResultId, Request.Status.accepted.toString());
        assertThat(status(handleResponseResult)).isEqualTo(OK);
        assertThat(contentAsString(handleResponseResult)).isEqualTo("\"" + BaseController.OK_STRING + "\"");

        JPA.withTransaction(() -> {
            Optional<Request> requestOptional = DbUtils.findEntityById(Request.class, createdResultId);
            assertThat(requestOptional.isPresent()).isTrue();

            Request request = requestOptional.get();
            assertThat(request.status).isEqualTo(Request.Status.accepted);
            assertThat(request.respondedTimeStamp).isPositive();

            long senderId = request.sender.userId;
            long receiverId = request.receiver.userId;

            String queryString = "select p from PrivateRoom p where (p.sender.userId = :senderId and p.senderInRoom = true) and (p.receiver.userId = :receiverId and p.receiverInRoom = true)";

            TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
                    .setParameter("senderId", senderId)
                    .setParameter("receiverId", receiverId);

            List<PrivateRoom> privateRoomList = query.getResultList();
            assertThat(privateRoomList).hasSize(1);

            PrivateRoom privateRoom = privateRoomList.get(0);

            assertThat(privateRoom.roomId).isPositive();
            assertThat(privateRoom.messages).isEmpty();
            assertThat(privateRoom.request.equals(request));
            assertThat(privateRoom.sender).isEqualTo(request.sender);
            assertThat(privateRoom.receiver).isEqualTo(request.receiver);
            assertThat(privateRoom.timeStamp).isPositive();
            assertThat(privateRoom.lastActivity).isEqualTo(privateRoom.timeStamp);
        });
    }

    @Test
    public void respondToRequestDeniedSuccess() throws JSONException {
        long createdResultId = adapter.getCreateRequestId();

        Result handleResponseResult = adapter.handleResponse(createdResultId, Request.Status.denied.toString());
        assertThat(status(handleResponseResult)).isEqualTo(OK);
        assertThat(contentAsString(handleResponseResult)).isEqualTo("\"" + BaseController.OK_STRING + "\"");

        JPA.withTransaction(() -> {
            Optional<Request> requestOptional = DbUtils.findEntityById(Request.class, createdResultId);
            assertThat(requestOptional.isPresent()).isTrue();

            Request request = requestOptional.get();
            assertThat(request.status).isEqualTo(Request.Status.denied);
            assertThat(request.respondedTimeStamp).isPositive();

            long senderId = request.sender.userId;
            long receiverId = request.receiver.userId;

            String queryString = "select p from PrivateRoom p where (p.sender.userId = :senderId and p.senderInRoom = true) and (p.receiver.userId = :receiverId and p.receiverInRoom = true)";

            TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
                    .setParameter("senderId", senderId)
                    .setParameter("receiverId", receiverId);

            List<PrivateRoom> privateRoomList = query.getResultList();
            assertThat(privateRoomList).isEmpty();
        });
    }

    @Test
    public void handleResponseBadRequestId() {
        int badId = 1;
        Result badIdResult = adapter.handleResponse(badId, Request.Status.accepted.toString());
        assertThat(status(badIdResult)).isEqualTo(NOT_FOUND);
        assertThat(contentAsString(badIdResult).contains(DbUtils.buildEntityNotFoundString(Request.ENTITY_NAME, badId)));
    }

    @Test
    public void handleResponseNoStatus() throws JSONException {
        long createdRequestId = adapter.getCreateRequestId();

        Result noStatusResult = adapter.handleResponse(createdRequestId, null);
        assertThat(status(noStatusResult)).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(noStatusResult)).isEqualTo(TestUtils.withQuotes("status is required"));
    }

    @Test
    public void handleResponsePending() throws JSONException {
        long createdRequestId = adapter.getCreateRequestId();

        Result noStatusResult = adapter.handleResponse(createdRequestId, Request.Status.pending.toString());
        assertThat(status(noStatusResult)).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(noStatusResult)).isEqualTo(TestUtils.withQuotes("Can't respond to a request with pending"));
    }

    @Test
    public void handleBadResponse() throws JSONException {
        long createdRequestId = adapter.getCreateRequestId();

        Result noStatusResult = adapter.handleResponse(createdRequestId, "mayonnaise");
        assertThat(status(noStatusResult)).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(noStatusResult)).isEqualTo(TestUtils.withQuotes("mayonnaise is not a valid response"));
    }

    @Test
    public void getStatus() throws JSONException {

        UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;
        long senderId = userAdapter.getCreateUserId();
        long receiverId = userAdapter.getCreateUserId();

        adapter.createRequest(senderId, receiverId, null);

        Result getStatusResult = adapter.getStatus(senderId, receiverId);
        assertThat(status(getStatusResult)).isEqualTo(OK);
        assertThat(contentAsString(getStatusResult)).isEqualTo(TestUtils.withQuotes(Request.Status.pending.toString()));
    }

    @Test
    public void getStatusNone() {
        Result noRequestResult = adapter.getStatus(1, 2);
        assertThat(status(noRequestResult)).isEqualTo(OK);
        assertThat(contentAsString(noRequestResult)).isEqualTo(TestUtils.withQuotes("none"));
    }

    @Test
    public void testConstructor() {
        RequestsController controller = new RequestsController();
        assertThat(controller).isNotNull();
    }
}
