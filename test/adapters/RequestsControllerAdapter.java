package adapters;


import controllers.routes;
import models.entities.Request;
import org.json.JSONException;
import org.json.JSONObject;
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static play.test.Helpers.*;

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

    public Result createRequest(Optional<Long> senderIdOptional, Optional<Long> receiverIdOptional, Optional<String> messageOptional) throws JSONException {
        Map<String, String> formData = new HashMap<>();

        senderIdOptional.ifPresent(senderId -> formData.put(SENDER_KEY, String.valueOf(senderId)));
        receiverIdOptional.ifPresent(receiverId -> formData.put(RECEIVER_KEY, String.valueOf(receiverId)));
        messageOptional.ifPresent(message -> formData.put(MESSAGE_KEY, message));

        return createRequest(Optional.of(formData), Optional.empty());
    }

    public Result createRequest(Optional<Map<String, String>> otherDataOptional, Optional<List<String>> removeFieldsOptional) throws JSONException {
        Map<String, String> formData = new HashMap<>();
        formData.put(MESSAGE_KEY, MESSAGE);

        otherDataOptional.ifPresent(data -> data.forEach(formData::put));

        if (!formData.containsKey(SENDER_KEY)) {
            long senderId = UsersControllerAdapter.INSTANCE.getCreateUserId(Optional.empty(), Optional.empty());
            formData.put(SENDER_KEY, String.valueOf(senderId));
        }
        if (!formData.containsKey(RECEIVER_KEY)) {
            long receiverId = UsersControllerAdapter.INSTANCE.getCreateUserId(Optional.empty(), Optional.empty());
            formData.put(RECEIVER_KEY, String.valueOf(receiverId));
        }

        removeFieldsOptional.ifPresent(removeFields -> removeFields.forEach(formData::remove));

        FakeRequest request = fakeRequest();
        request.withFormUrlEncodedBody(formData);
        return callAction(routes.ref.RequestsController.createRequest(), request);
    }

    public long getCreateRequestId(Optional<Map<String, String>> otherData, Optional<List<String>> removeFields) throws JSONException {
        Result createResult = createRequest(otherData, removeFields);
        JSONObject createJson = new JSONObject(contentAsString(createResult));
        return createJson.getLong(ID_KEY);
    }

    public Result getRequests(long receiverId) {
        return callAction(routes.ref.RequestsController.getRequests(receiverId), fakeRequest());
    }

    public Result showRequest(long requestId) {
        return callAction(routes.ref.RequestsController.showRequest(requestId), fakeRequest());
    }

    public Result updateRequest(long requestId, Map<String, String> formData) {
        FakeRequest request = fakeRequest();
        request.withFormUrlEncodedBody(formData);
        return callAction(routes.ref.RequestsController.updateRequest(requestId), request);
    }

    public Result deleteRequest(long requestId) {
        return callAction(routes.ref.RequestsController.deleteRequest(requestId), fakeRequest());
    }

    public Result checkExists(long senderId, long receiverId) {
        return callAction(routes.ref.RequestsController.doesExist(senderId, receiverId), fakeRequest());
    }
}
