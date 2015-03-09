package integration;

import adapters.RequestsControllerAdapter;
import adapters.UsersControllerAdapter;
import models.entities.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import play.mvc.Result;

import java.util.*;

import static adapters.RequestsControllerAdapter.*;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.status;


public class RequestsControllerTest extends AbstractControllerTest {

    RequestsControllerAdapter adapter = RequestsControllerAdapter.INSTANCE;

    @Test
    public void createRequestSuccess() throws JSONException {
        UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;
        Result senderCreateResult = userAdapter.createUser(Optional.empty(), Optional.empty());
        JSONObject senderCreateJson = new JSONObject(contentAsString(senderCreateResult));

        Result receiverCreateResult = userAdapter.createUser(Optional.empty(), Optional.empty());
        JSONObject receiverCreateJson = new JSONObject(contentAsString(receiverCreateResult));

        long senderId = senderCreateJson.getLong(UsersControllerAdapter.ID_KEY);
        long receiverId = receiverCreateJson.getLong(UsersControllerAdapter.ID_KEY);
        Result createResult = adapter.createRequest(Optional.of(senderId), Optional.of(receiverId), Optional.of(MESSAGE));
        assertThat(status(createResult)).isEqualTo(OK);

        JSONObject createJson = new JSONObject(contentAsString(createResult));
        assertThat(createJson.getLong(ID_KEY)).isPositive();
        assertThat(createJson.getJSONObject(SENDER_KEY).toString()).isEqualTo(senderCreateJson.toString());
        assertThat(createJson.getJSONObject(RECEIVER_KEY).toString()).isEqualTo(receiverCreateJson.toString());
        assertThat(createJson.getString(MESSAGE_KEY)).isEqualTo(MESSAGE);
        assertThat(createJson.getString(STATUS_KEY)).isEqualTo(STATUS);
        long timeStamp = createJson.getLong(TIMESTAMP_KEY);
        assertThat(timeStamp).isPositive();
        assertThat(createJson.getLong(RESPONDED_TIMESTAMP_KEY)).isZero();

        Result showResult = adapter.showRequest(createJson.getLong(ID_KEY));
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJson = new JSONObject(contentAsString(showResult));
        assertThat(showJson.getLong(ID_KEY)).isPositive();
        assertThat(showJson.getJSONObject(SENDER_KEY).toString()).isEqualTo(senderCreateJson.toString());
        assertThat(showJson.getJSONObject(RECEIVER_KEY).toString()).isEqualTo(receiverCreateJson.toString());
        assertThat(showJson.getString(MESSAGE_KEY)).isEqualTo(MESSAGE);
        assertThat(showJson.getString(STATUS_KEY)).isEqualTo(STATUS);
        assertThat(showJson.getLong(TIMESTAMP_KEY)).isEqualTo(timeStamp);
        assertThat(showJson.getLong(RESPONDED_TIMESTAMP_KEY)).isZero();
    }

    @Test
    public void createRequestNoSender() throws JSONException {
        Result noSenderResult = adapter.createRequest(Optional.empty(), Optional.of(Arrays.asList(SENDER_KEY)));
        assertThat(status(noSenderResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRequestNoReceiver() throws JSONException {
        Result noReceiverResult = adapter.createRequest(Optional.empty(), Optional.of(Arrays.asList(RECEIVER_KEY)));
        assertThat(status(noReceiverResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRequestBadSender() throws JSONException {
        Result badSenderResult = adapter.createRequest(Optional.of(500L), Optional.empty(), Optional.of(MESSAGE));
        assertThat(status(badSenderResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRequestBadReceiver() throws JSONException {
        Result badReceiverResult = adapter.createRequest(Optional.empty(), Optional.of(500L), Optional.of(MESSAGE));
        assertThat(status(badReceiverResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRequestNegativeId() throws JSONException {
        Result badReceiverResult = adapter.createRequest(Optional.empty(), Optional.of(-1L), Optional.of(MESSAGE));
        assertThat(status(badReceiverResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRequestNotNumericId() throws JSONException {
        Map<String, String> formData = new HashMap<>();
        formData.put(SENDER_KEY, "NotALong");
        Result badReceiverResult = adapter.createRequest(Optional.of(formData), Optional.empty());
        assertThat(status(badReceiverResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void getRequestsByReceiver() throws JSONException {
        Result createResult = adapter.createRequest(Optional.empty(), Optional.empty());
        JSONObject receiverJson = new JSONObject(contentAsString(createResult)).getJSONObject(RECEIVER_KEY);
        long receiverId = receiverJson.getLong(UsersControllerAdapter.ID_KEY);

        Result requestsByReceiverId = adapter.getRequests(receiverId);
        assertThat(status(requestsByReceiverId)).isEqualTo(OK);

        JSONArray requestsByReceiverJson = new JSONArray(contentAsString(requestsByReceiverId));
        assertThat(requestsByReceiverJson.length()).isEqualTo(1);
    }

    @Test
    public void showRequestSuccess() throws JSONException {
        long createdId = adapter.getCreateRequestId(Optional.empty(), Optional.empty());

        Result showResult = adapter.showRequest(createdId);
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJsonResult = new JSONObject(contentAsString(showResult));
        assertThat(showJsonResult.getLong(ID_KEY)).isEqualTo(createdId);
        assertThat(showJsonResult.getJSONObject(SENDER_KEY)).isNotNull();
        assertThat(showJsonResult.getJSONObject(RECEIVER_KEY)).isNotNull();
        assertThat(showJsonResult.getString(MESSAGE_KEY)).isEqualTo(MESSAGE);
        assertThat(showJsonResult.getString(STATUS_KEY)).isEqualTo(STATUS);
        assertThat(showJsonResult.getLong(TIMESTAMP_KEY)).isPositive();
        assertThat(showJsonResult.getLong(RESPONDED_TIMESTAMP_KEY)).isZero();
    }

    @Test
    public void showRequestBadId() throws JSONException {
        Result showBadId = adapter.showRequest(1);
        assertThat(status(showBadId)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void updateRequestSuccess() throws JSONException {
        long createdResultId = adapter.getCreateRequestId(Optional.empty(), Optional.empty());

        Map<String, String> updateData = new HashMap<>();
        updateData.put(ID_KEY, "-1");
        updateData.put(SENDER_KEY, "-1");
        updateData.put(RECEIVER_KEY, "-1");
        updateData.put(STATUS_KEY, Request.Status.accepted.toString());

        Result updateResult = adapter.updateRequest(createdResultId, updateData);
        JSONObject updateJson = new JSONObject(contentAsString(updateResult));
        assertThat(updateJson.getLong(ID_KEY)).isNotEqualTo(-1);
        assertThat(updateJson.getJSONObject(SENDER_KEY).getString(UsersControllerAdapter.ID_KEY)).isNotEqualTo("-1");
        assertThat(updateJson.getJSONObject(RECEIVER_KEY).getString(UsersControllerAdapter.ID_KEY)).isNotEqualTo("-1");
        assertThat(updateJson.getString(STATUS_KEY)).isEqualTo(Request.Status.accepted.toString());
    }

    @Test
    public void updateRequestBadId() {
        Result updateBadId = adapter.updateRequest(1, Collections.emptyMap());
        assertThat(status(updateBadId)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void deleteRequestSuccess() throws JSONException {
        long createId = adapter.getCreateRequestId(Optional.empty(), Optional.empty());

        Result deleteResult = adapter.deleteRequest(createId);
        assertThat(status(deleteResult)).isEqualTo(OK);

        Result showResult = adapter.showRequest(createId);
        assertThat(status(showResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void deleteRequestBadId() {
        Result deleteBadId = adapter.deleteRequest(1);
        assertThat(status(deleteBadId)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void requestDoesNotExistSuccess() throws JSONException {
        UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;
        long user1 = userAdapter.getCreateUserId(Optional.empty(), Optional.empty());
        long user2 = userAdapter.getCreateUserId(Optional.empty(), Optional.empty());
        long user3 = userAdapter.getCreateUserId(Optional.empty(), Optional.empty());

        Result checkExistsResult = adapter.checkExists(user1, user2);
        assertThat(status(checkExistsResult)).isEqualTo(OK);
        assertThat(contentAsString(checkExistsResult)).isEqualTo("false");

        adapter.createRequest(Optional.of(user1), Optional.of(user3), Optional.empty());
        adapter.createRequest(Optional.of(user3), Optional.of(user2), Optional.empty());

        checkExistsResult = adapter.checkExists(user1, user2);
        assertThat(status(checkExistsResult)).isEqualTo(OK);
        assertThat(contentAsString(checkExistsResult)).isEqualTo("false");
    }

    @Test
    public void requestExistsSuccess() throws JSONException {
        UsersControllerAdapter userAdapter = UsersControllerAdapter.INSTANCE;

        long user1 = userAdapter.getCreateUserId(Optional.empty(), Optional.empty());
        long user2 = userAdapter.getCreateUserId(Optional.empty(), Optional.empty());

        adapter.createRequest(Optional.of(user1), Optional.of(user2), Optional.empty());

        Result checkExistsResult = adapter.checkExists(user1, user2);
        assertThat(status(checkExistsResult)).isEqualTo(OK);
        assertThat(contentAsString(checkExistsResult)).isEqualTo("true");
    }

}
