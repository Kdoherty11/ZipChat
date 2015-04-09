package integration;

import adapters.RoomsControllerAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import play.mvc.Result;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.status;


public class PublicRoomsControllerTest extends AbstractControllerTest {

    private static final RoomsControllerAdapter adapter = RoomsControllerAdapter.INSTANCE;

    @Test
    public void createRoomSuccess() throws JSONException {
        Result createResult = adapter.createRoom();
        assertThat(status(createResult)).isEqualTo(CREATED);

        JSONObject createJson = new JSONObject(contentAsString(createResult));
        assertThat(createJson.getLong(RoomsControllerAdapter.ID_KEY)).isNotNull();
        assertThat(createJson.getString(RoomsControllerAdapter.NAME_KEY)).isEqualTo(RoomsControllerAdapter.NAME);
        assertThat(createJson.getLong(RoomsControllerAdapter.LAT_KEY)).isEqualTo(RoomsControllerAdapter.LAT);
        assertThat(createJson.getLong(RoomsControllerAdapter.LON_KEY)).isEqualTo(RoomsControllerAdapter.LON);

        Result showResult = adapter.showRoom(createJson.getLong(RoomsControllerAdapter.ID_KEY));
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJson = new JSONObject(contentAsString(showResult));
        assertThat(showJson.getLong(RoomsControllerAdapter.ID_KEY)).isNotNull();
    }

    @Test
    public void createRoomNoName() {
        Result noNameResult = adapter.createRoom(null, RoomsControllerAdapter.NAME_KEY);
        assertThat(status(noNameResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRoomNoLatitude() {

        Result noLatResult = adapter.createRoom(null, RoomsControllerAdapter.LAT_KEY);
        assertThat(status(noLatResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRoomNoLongitude() {
        Result noLonResult = adapter.createRoom(null, RoomsControllerAdapter.LON_KEY);
        assertThat(status(noLonResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRoomNoRadius() {
        Result noRadiusResult = adapter.createRoom(null, RoomsControllerAdapter.RADIUS_KEY);
        assertThat(status(noRadiusResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void getRoomsSuccess() throws JSONException {
        adapter.createRoom();

        Result allRoomssResult = adapter.getRooms();
        assertThat(status(allRoomssResult)).isEqualTo(OK);

        JSONArray allRoomsJson = new JSONArray(contentAsString(allRoomssResult));
        assertThat(allRoomsJson.length()).isEqualTo(1);
    }

    @Test
    public void showRoomSuccess() throws JSONException {
        long createdId = adapter.getCreateRoomId();

        Result showResult = adapter.showRoom(createdId);
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJsonResult = new JSONObject(contentAsString(showResult));
        assertThat(showJsonResult.getLong(RoomsControllerAdapter.ID_KEY)).isEqualTo(createdId);
        assertThat(showJsonResult.getString(RoomsControllerAdapter.NAME_KEY)).isEqualTo(RoomsControllerAdapter.NAME);
        assertThat(showJsonResult.getLong(RoomsControllerAdapter.LAT_KEY) == RoomsControllerAdapter.LAT);
        assertThat(showJsonResult.getLong(RoomsControllerAdapter.LON_KEY) == RoomsControllerAdapter.LON);
        assertThat(showJsonResult.getInt(RoomsControllerAdapter.RADIUS_KEY) == RoomsControllerAdapter.RADIUS);
    }

    @Test
    public void showRoomBadId() throws JSONException {
        Result showBadId = adapter.showRoom(1);
        assertThat(status(showBadId)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void updateRoomSuccess() throws JSONException {
        long createdId = adapter.getCreateRoomId();

        String updateRoomId = "-1";
        String updateName = "NEW NAME";
        long updateLat = 99999;
        long updateLon = 99999;
        long updateRadius = 99999;
        Integer updateScore = 5;

        Map<String, String> updateFields = new HashMap<>();
        updateFields.put(RoomsControllerAdapter.ID_KEY, updateRoomId);
        updateFields.put(RoomsControllerAdapter.SCORE_KEY, String.valueOf(updateScore));
        updateFields.put(RoomsControllerAdapter.LAT_KEY, String.valueOf(updateLat));
        updateFields.put(RoomsControllerAdapter.LON_KEY, String.valueOf(updateLon));
        updateFields.put(RoomsControllerAdapter.RADIUS_KEY, String.valueOf(updateRadius));
        updateFields.put(RoomsControllerAdapter.NAME_KEY, updateName);

        Result updateResult = adapter.updateRoom(createdId, updateFields);
        assertThat(status(updateResult)).isEqualTo(OK);

        JSONObject updateJson = new JSONObject(contentAsString(updateResult));
        assertThat(updateJson.getLong(RoomsControllerAdapter.ID_KEY)).isEqualTo(createdId);
        assertThat(updateJson.getInt(RoomsControllerAdapter.SCORE_KEY)).isEqualTo(updateScore);
        assertThat(updateJson.getLong(RoomsControllerAdapter.LAT_KEY)).isEqualTo(RoomsControllerAdapter.LAT); //check that this was not updated
        assertThat(updateJson.getLong(RoomsControllerAdapter.LON_KEY)).isEqualTo(RoomsControllerAdapter.LON); //check that this was not updated
        assertThat(updateJson.getString(RoomsControllerAdapter.NAME_KEY)).isEqualTo(RoomsControllerAdapter.NAME); //check that this was not updated

        Result showResult = adapter.showRoom(createdId);
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJson = new JSONObject(contentAsString(showResult));
        assertThat(showJson.getInt(RoomsControllerAdapter.SCORE_KEY)).isEqualTo(updateScore);
        assertThat(showJson.getString(RoomsControllerAdapter.NAME_KEY)).isEqualTo(RoomsControllerAdapter.NAME);
        assertThat(showJson.getLong(RoomsControllerAdapter.LAT_KEY)).isEqualTo(RoomsControllerAdapter.LAT);
        assertThat(showJson.getLong(RoomsControllerAdapter.LON_KEY)).isEqualTo(RoomsControllerAdapter.LON);
    }

    @Test
    public void updateRoomBadId() {
        Result updateBadId = adapter.updateRoom(1, Collections.emptyMap());
        assertThat(status(updateBadId)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void deleteRoomSuccess() throws JSONException {
        long createId = adapter.getCreateRoomId();

        Result deleteResult = adapter.deleteRoom(createId);
        assertThat(status(deleteResult)).isEqualTo(OK);

        Result showResult = adapter.showRoom(createId);
        assertThat(status(showResult)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void deleteRoomBadId() {
        Result deleteBadId = adapter.deleteRoom(1);
        assertThat(status(deleteBadId)).isEqualTo(NOT_FOUND);
    }
}
