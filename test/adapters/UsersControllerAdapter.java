package adapters;

import controllers.routes;
import org.jetbrains.annotations.Nullable;
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

public enum UsersControllerAdapter {

    INSTANCE;

    public static final String ID_KEY = "userId";
    public static final String NAME_KEY = "name";
    public static final String FB_KEY = "facebookId";
    public static final String REG_KEY = "registrationId";
    public static final String PLATFORM_KEY = "platform";

    public static final String NAME = "Controller Test";
    public static final String FB_ID = "1111111111";
    public static final String REG_ID = "2222222222";
    public static final String PLATFORM = "android";

    public Result createUser() {
        return createUser(null, null);
    }

    public Result createUser(@Nullable Map<String, String> data, @Nullable List<String> removeFields) {
        Map<String, String> formData = new HashMap<>();
        formData.put(NAME_KEY, NAME);
        formData.put(FB_KEY, FB_ID);
        formData.put(REG_KEY, REG_ID);
        formData.put(PLATFORM_KEY, PLATFORM);

        if (data != null) {
            data.forEach(formData::put);
        }
        if (removeFields != null) {
            removeFields.forEach(formData::remove);
        }

        FakeRequest request = fakeRequest();
        request.withFormUrlEncodedBody(formData);
        return callAction(routes.ref.UsersController.createUser(), request);
    }

    public long getCreateUserId() throws JSONException {
        return getCreateUserId(null, null);
    }

    public long getCreateUserId(@Nullable Map<String, String> data, @Nullable List<String> removeFields) throws JSONException {
        return getUserIdFromResult(createUser(data, removeFields));
    }

    public long getUserIdFromResult(Result createResult) throws JSONException {
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
