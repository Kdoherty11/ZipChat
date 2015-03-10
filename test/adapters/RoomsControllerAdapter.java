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

import static play.test.Helpers.*;
/**
 * Created by zacharywebert on 3/8/15.
 */
public enum RoomsControllerAdapter {

    INSTANCE;

    public static final String ID_KEY = "roomId";
    public static final String NAME_KEY = "name";
    public static final String LAT_KEY = "latitude";
    public static final String LON_KEY = "longitude";
    public static final String RADIUS_KEY = "radius";
    public static final String SCORE_KEY = "score";

    public static final String NAME = "Room Test";
    public static final long LAT = 1;
    public static final long LON = 1;
    public static final long RADIUS = 100;

    public static Result createRoom(Optional<Map<String, String>> otherDataOptional, Optional<List<String>> removeFieldsOptional) {
        Map<String, String> formData = new HashMap<>();
        formData.put(NAME_KEY, NAME);
        formData.put(LAT_KEY, String.valueOf(LAT));
        formData.put(LON_KEY, String.valueOf(LON));
        formData.put(RADIUS_KEY, String.valueOf(RADIUS));

        otherDataOptional.ifPresent(otherData -> otherData.forEach(formData::put));
        removeFieldsOptional.ifPresent(removeFields -> removeFields.forEach(formData::remove));

        FakeRequest request = fakeRequest();
        request.withFormUrlEncodedBody(formData);
        return callAction(routes.ref.PublicRoomsController.createRoom(), request);
    }

    public static long getCreateRoomId(Optional<Map<String, String>> otherData, Optional<List<String>> removeFields) throws JSONException {
        Result createResult = createRoom(otherData, removeFields);
        JSONObject createJson = new JSONObject(contentAsString(createResult));
        return createJson.getLong(ID_KEY);
    }

    public static Result getRooms() {
        return callAction(routes.ref.PublicRoomsController.getRooms(), fakeRequest());
    }

    public static Result showRoom(long roomId) {
        return callAction(routes.ref.PublicRoomsController.showRoom(roomId), fakeRequest());
    }

    public static Result updateRoom(long roomId, Map<String, String> formData) {
        FakeRequest request = fakeRequest();
        request.withFormUrlEncodedBody(formData);
        return callAction(routes.ref.PublicRoomsController.updateRoom(roomId), request);
    }

    public static Result deleteRoom(long roomId) {
        return callAction(routes.ref.PublicRoomsController.deleteRoom(roomId), fakeRequest());
    }

    public static Result createSubscription(long roomId, long userId) {

        Map<String, String> formData = new HashMap<>();
        formData.put(UsersControllerAdapter.ID_KEY, String.valueOf(userId));

        FakeRequest request = fakeRequest();
        request.withFormUrlEncodedBody(formData);
        return callAction(routes.ref.PublicRoomsController.createSubscription(roomId), request);
    }

    public static Result getSubscriptions(long roomId) {
        return callAction(routes.ref.PublicRoomsController.getSubscriptions(roomId), fakeRequest());
    }
}
