package adapters;


import controllers.routes;
import models.entities.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.*;

import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;

public enum RequestsControllerAdapter {

    INSTANCE;

    public static final String ID_KEY = "id";
    public static final String SENDER_KEY = "sender";
    public static final String RECEIVER_KEY = "receiver";
    public static final String MESSAGE_KEY = "message";
    public static final String STATUS_KEY = "status";
    public static final String TIMESTAMP_KEY = "timeStamp";
    public static final String RESPONDED_TIMESTAMP_KEY = "respondedTimeStamp";

    public static final String MESSAGE = "Hey, whats up?";
    public static final String STATUS = Request.Status.pending.toString();

    public Result createRequest() throws JSONException {
        return createRequest(null);
    }

    public Result createRequest(@Nullable Long senderId, @Nullable Long receiverId, @Nullable String message) throws JSONException {
        Map<String, String> formData = new HashMap<>();

        if (senderId != null) {
            formData.put(SENDER_KEY, String.valueOf(senderId));
        }

        if (receiverId != null) {
            formData.put(RECEIVER_KEY, String.valueOf(receiverId));
        }

        if (message != null) {
            formData.put(MESSAGE_KEY, message);
        }

        return createRequest(formData);
    }

    public Result createRequest(@Nullable Map<String, String> otherData, String... removeFields) throws JSONException {
        Map<String, String> formData = new HashMap<>();
        formData.put(MESSAGE_KEY, MESSAGE);

        if (otherData != null) {
            otherData.forEach(formData::put);
        }

        if (!formData.containsKey(SENDER_KEY)) {
            long senderId = UsersControllerAdapter.INSTANCE.getCreateUserId();
            formData.put(SENDER_KEY, String.valueOf(senderId));
        }

        if (!formData.containsKey(RECEIVER_KEY)) {
            long receiverId = UsersControllerAdapter.INSTANCE.getCreateUserId();
            formData.put(RECEIVER_KEY, String.valueOf(receiverId));
        }

        Arrays.asList(removeFields).forEach(formData::remove);

        FakeRequest request = fakeRequest();
        request.withFormUrlEncodedBody(formData);
        return callAction(routes.ref.RequestsController.createRequest(), request);
    }

    public long getCreateRequestId() throws JSONException {
        return getCreateRequestId(null);
    }

    public long getCreateRequestId(@Nullable Map<String, String> otherData, String... removeFields) throws JSONException {
        Result createResult = createRequest(otherData, removeFields);
        JSONObject createJson = new JSONObject(contentAsString(createResult));
        return createJson.getLong(ID_KEY);
    }

    public Result getRequestsByReceiver(long receiverId) {
        return callAction(routes.ref.RequestsController.getRequestsByReceiver(receiverId), fakeRequest());
    }

    public Result handleResponse(long requestId, String response) {
        FakeRequest request = fakeRequest();
        Optional.ofNullable(response).ifPresent(st -> {
            Map<String, String> formData = new HashMap<>();
            formData.put(STATUS_KEY, response);
            request.withFormUrlEncodedBody(formData);
        });
        return callAction(routes.ref.RequestsController.handleResponse(requestId), request);
    }

    public Result getStatus(long senderId, long receiverId) {
        return callAction(routes.ref.RequestsController.getStatus(senderId, receiverId), fakeRequest());
    }
}
