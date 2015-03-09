package integration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import play.mvc.Result;

import java.util.*;

import static adapters.RoomsControllerAdapter.*;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.status;

/**
 * Created by zacharywebert on 3/8/15.
 */
public class RoomsControllerTest extends AbstractControllerTest {

    @Test
    public void createRoomSuccess() throws JSONException {
        Result createResult = createRoom(Optional.empty(), Optional.empty());
        assertThat(status(createResult)).isEqualTo(OK);

        JSONObject createJson = new JSONObject(contentAsString(createResult));
        assertThat(createJson.getLong(ID_KEY)).isNotNull();
        assertThat(createJson.getString(NAME_KEY)).isEqualTo(NAME);
        assertThat(createJson.getLong(LAT_KEY)).isEqualTo(LAT);
        assertThat(createJson.getLong(LON_KEY)).isEqualTo(LON);

        Result showResult = showRoom(createJson.getLong(ID_KEY));
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJson = new JSONObject(contentAsString(showResult));
        assertThat(showJson.getLong(ID_KEY)).isNotNull();
    }

    @Test
    public void createRoomNoName() {
        Result noNameResult = createRoom(Optional.empty(), Optional.of(Arrays.asList(NAME_KEY)));
        assertThat(status(noNameResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRoomNoLatitude() {

        Result noLatResult = createRoom(Optional.empty(), Optional.of(Arrays.asList(LAT_KEY)));
        assertThat(status(noLatResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRoomNoLongitude() {
        Result noLonResult = createRoom(Optional.empty(), Optional.of(Arrays.asList(LON_KEY)));
        assertThat(status(noLonResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createRoomNoRadius() {
        Result noRadiusResult = createRoom(Optional.empty(), Optional.of(Arrays.asList(RADIUS_KEY)));
        assertThat(status(noRadiusResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void getRoomsSuccess() throws JSONException {
        createRoom(Optional.empty(), Optional.empty());

        Result allRoomssResult = getRooms();
        assertThat(status(allRoomssResult)).isEqualTo(OK);

        JSONArray allRoomsJson = new JSONArray(contentAsString(allRoomssResult));
        assertThat(allRoomsJson.length()).isEqualTo(1);
    }

    @Test
    public void showRoomSuccess() throws JSONException {
        long createdId = getCreateRoomId(Optional.empty(), Optional.empty());

        Result showResult = showRoom(createdId);
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJsonResult = new JSONObject(contentAsString(showResult));
        assertThat(showJsonResult.getLong(ID_KEY)).isEqualTo(createdId);
        assertThat(showJsonResult.getString(NAME_KEY)).isEqualTo(NAME);
        assertThat(showJsonResult.getLong(LAT_KEY) == LAT);
        assertThat(showJsonResult.getLong(LON_KEY) == LON);
        assertThat(showJsonResult.getInt(RADIUS_KEY) == RADIUS);
    }

    @Test
    public void showRoomBadId() throws JSONException {
        Result showBadId = showRoom(1);
        assertThat(status(showBadId)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void updateRoomSuccess() throws JSONException {
        long createdId = getCreateRoomId(Optional.empty(), Optional.empty());

        String updateRoomId = "-1";
        String updateName = "NEW NAME";
        long updateLat = 99999;
        long updateLon = 99999;
        long updateRadius = 99999;
        Integer updateScore = 5;

        Map<String, String> updateFields = new HashMap<>();
        updateFields.put(ID_KEY, updateRoomId);
        updateFields.put(SCORE_KEY, String.valueOf(updateScore));
        updateFields.put(LAT_KEY, String.valueOf(updateLat));
        updateFields.put(LON_KEY, String.valueOf(updateLon));
        updateFields.put(RADIUS_KEY, String.valueOf(updateRadius));
        updateFields.put(NAME_KEY, updateName);

        Result updateResult = updateRoom(createdId, updateFields);
        assertThat(status(updateResult)).isEqualTo(OK);

        JSONObject updateJson = new JSONObject(contentAsString(updateResult));
        assertThat(updateJson.getLong(ID_KEY)).isEqualTo(createdId);
        assertThat(updateJson.getInt(SCORE_KEY)).isEqualTo(updateScore);
        assertThat(updateJson.getLong(LAT_KEY)).isEqualTo(LAT); //check that this was not updated
        assertThat(updateJson.getLong(LON_KEY)).isEqualTo(LON); //check that this was not updated
        assertThat(updateJson.getString(NAME_KEY)).isEqualTo(NAME); //check that this was not updated


        Result showResult = showRoom(createdId);
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJson = new JSONObject(contentAsString(showResult));
        assertThat(showJson.getInt(SCORE_KEY)).isEqualTo(updateScore);
        assertThat(showJson.getString(NAME_KEY)).isEqualTo(NAME);
        assertThat(showJson.getLong(LAT_KEY)).isEqualTo(LAT);
        assertThat(showJson.getLong(LON_KEY)).isEqualTo(LON);
    }

    @Test
    public void updateRoomBadId() {
        Result updateBadId = updateRoom(1, Collections.emptyMap());
        assertThat(status(updateBadId)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void deleteRoomSuccess() throws JSONException {
        long createId = getCreateRoomId(Optional.empty(), Optional.empty());

        Result deleteResult = deleteRoom(createId);
        assertThat(status(deleteResult)).isEqualTo(OK);

        Result showResult = showRoom(createId);
        assertThat(status(showResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void deleteRoomBadId() {
        Result deleteBadId = deleteRoom(1);
        assertThat(status(deleteBadId)).isEqualTo(BAD_REQUEST);
    }
}
