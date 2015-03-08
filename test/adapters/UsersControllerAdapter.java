package adapters;

import controllers.routes;
import org.json.JSONException;
import org.json.JSONObject;
import play.mvc.Result;
import play.test.FakeRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;
import static integration.UsersControllerTest.*;

public class UsersControllerAdapter {

    public Result createUser(Optional<Map<String, String>> otherDataOptional, Optional<List<String>> removeFieldsOptional) {
        Map<String, String> formData = new HashMap<>();
        formData.put(NAME_KEY, NAME);
        formData.put(FB_KEY, FB_ID);
        formData.put(REG_KEY, REG_ID);
        formData.put(PLATFORM_KEY, PLATFORM);

        if (otherDataOptional.isPresent()) {
            otherDataOptional.get().forEach(formData::put);
        }
        if (removeFieldsOptional.isPresent()) {
            removeFieldsOptional.get().forEach(formData::remove);
        }

        FakeRequest request = fakeRequest();
        request.withFormUrlEncodedBody(formData);
        return callAction(routes.ref.UsersController.createUser(), request);
    }

    public long getCreateUserId(Optional<Map<String, String>> otherData, Optional<List<String>> removeFields) throws JSONException {
        Result createResult = createUser(otherData, removeFields);
        JSONObject createJson = new JSONObject(contentAsString(createResult));
        return createJson.getLong(ID_KEY);
    }

    public Result getUsers() {
        return callAction(routes.ref.UsersController.getUsers(), fakeRequest());
    }

    public Result showUser(long userId) {
        return callAction(routes.ref.UsersController.showUser(userId), fakeRequest());
    }

    public Result updateUser(long userId, Map<String, String> formData) {
        FakeRequest request = fakeRequest();
        request.withFormUrlEncodedBody(formData);
        return callAction(routes.ref.UsersController.updateUser(userId), request);
    }

    public Result deleteUser(long userId) {
        return callAction(routes.ref.UsersController.deleteUser(userId), fakeRequest());
    }

    public Result sendNotification(long userId, Map<String, String> formData) {
        FakeRequest request = fakeRequest();
        request.withFormUrlEncodedBody(formData);
        return callAction(routes.ref.UsersController.sendNotification(userId), request);
    }
}