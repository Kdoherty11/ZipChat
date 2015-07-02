package controllers;

import factories.ObjectFactory;
import models.entities.PublicRoom;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Result;
import play.test.WithApplication;
import security.SecurityHelper;
import services.PublicRoomService;
import services.UserService;
import utils.JsonArrayIterator;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.when;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
public class PublicRoomsControllerTest extends WithApplication {

    private PublicRoomsController controller;
    private ObjectFactory<PublicRoom> publicRoomFactory;

    @Mock
    private PublicRoomService publicRoomService;

    @Mock
    private MessagesController messagesController;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private UserService userService;

    @Before
    public void setUp() throws Exception {
        publicRoomFactory = new ObjectFactory<>(PublicRoom.class);
        controller = new PublicRoomsController(publicRoomService, messagesController,
                securityHelper, userService);

        final GlobalSettings global = new GlobalSettings() {

            @Override
            public <T> T getControllerInstance(Class<T> clazz) {
                if (clazz.getSuperclass() == Action.class) {
                    return null;
                }

                return (T) controller;
            }

        };

        start(fakeApplication(global));
    }

    @Test
    public void testGetGeoRoomsEmpty() throws JSONException {
        List<PublicRoom> rooms = new ArrayList<>();
        when(publicRoomService.allInGeoRange(anyDouble(), anyDouble())).thenReturn(rooms);
        final Result result = route(fakeRequest(GET, "/publicRooms?lat=2.0&lon=43.0"));
        assertThat(status(result)).isEqualTo(OK);
        JSONArray resultRooms = new JSONArray(contentAsString(result));
        assertThat(resultRooms.length()).isZero();
    }

    @Test
    public void testGeoRooms() throws Throwable {
        List<PublicRoom> rooms = publicRoomFactory.createList(3, false);
        when(publicRoomService.allInGeoRange(anyDouble(), anyDouble())).thenReturn(rooms);
        final Result result = route(fakeRequest(GET, "/publicRooms?lat=2.0&lon=43.0"));

        assertThat(status(result)).isEqualTo(OK);
        JSONArray resultRooms = new JSONArray(contentAsString(result));
        assertThat(resultRooms.length()).isEqualTo(3);
        new JsonArrayIterator(resultRooms).forEach(PublicRoomsControllerTest::validatePublicRoom);
    }

    @Test
    public void testGeoRoomsNoLat() {
        final Result result = route(fakeRequest(GET, "/publicRooms?lon=43.0"));
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void testGeoRoomsNoLon() {
        final Result result = route(fakeRequest(GET, "/publicRooms?lat=43.0"));
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void testGeoRoomsNoParams() {
        final Result result = route(fakeRequest(GET, "/publicRooms"));
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

    private static void validatePublicRoom(JSONObject publicRoom) {
        try {
            final long roomId = publicRoom.getLong("roomId");
            final String name = publicRoom.getString("name");
            final double latitude = publicRoom.getDouble("latitude");
            final double longitude = publicRoom.getDouble("longitude");
            final int radius = publicRoom.getInt("radius");
            final long createdAt = publicRoom.getLong("createdAt");
            final long lastActivity = publicRoom.getLong("lastActivity");

            assertThat(roomId).isPositive();
            assertThat(name).isNotEmpty();
            assertThat(latitude).isGreaterThanOrEqualTo(-90.0).isLessThanOrEqualTo(90.0);
            assertThat(longitude).isGreaterThanOrEqualTo(-180.0).isLessThanOrEqualTo(180.0);
            assertThat(radius).isPositive();
            assertThat(createdAt).isGreaterThan(0);
            assertThat(lastActivity).isGreaterThanOrEqualTo(createdAt);
        } catch (JSONException e) {
            throw new RuntimeException("Problem parsing public room json!", e);
        }
    }




//    @Test
//    public void createRoomSuccess() throws JSONException {
//        Result createResult = adapter.createRoom();
//        assertThat(status(createResult)).isEqualTo(CREATED);
//
//        JSONObject createJson = new JSONObject(contentAsString(createResult));
//        assertThat(createJson.getLong(RoomsControllerAdapter.ID_KEY)).isNotNull();
//        assertThat(createJson.getString(RoomsControllerAdapter.NAME_KEY)).isEqualTo(RoomsControllerAdapter.NAME);
//        assertThat(createJson.getLong(RoomsControllerAdapter.LAT_KEY)).isEqualTo(RoomsControllerAdapter.LAT);
//        assertThat(createJson.getLong(RoomsControllerAdapter.LON_KEY)).isEqualTo(RoomsControllerAdapter.LON);
//
//        Result showResult = adapter.showRoom(createJson.getLong(RoomsControllerAdapter.ID_KEY));
//        assertThat(status(showResult)).isEqualTo(OK);
//
//        JSONObject showJson = new JSONObject(contentAsString(showResult));
//        assertThat(showJson.getLong(RoomsControllerAdapter.ID_KEY)).isNotNull();
//    }
//
//    @Test
//    public void createRoomNoName() {
//        Result noNameResult = adapter.createRoom(null, RoomsControllerAdapter.NAME_KEY);
//        assertThat(status(noNameResult)).isEqualTo(BAD_REQUEST);
//    }
//
//    @Test
//    public void createRoomNoLatitude() {
//
//        Result noLatResult = adapter.createRoom(null, RoomsControllerAdapter.LAT_KEY);
//        assertThat(status(noLatResult)).isEqualTo(BAD_REQUEST);
//    }
//
//    @Test
//    public void createRoomNoLongitude() {
//        Result noLonResult = adapter.createRoom(null, RoomsControllerAdapter.LON_KEY);
//        assertThat(status(noLonResult)).isEqualTo(BAD_REQUEST);
//    }
//
//    @Test
//    public void createRoomNoRadius() {
//        Result noRadiusResult = adapter.createRoom(null, RoomsControllerAdapter.RADIUS_KEY);
//        assertThat(status(noRadiusResult)).isEqualTo(BAD_REQUEST);
//    }
//
//    @Test
//    public void getRoomsSuccess() throws JSONException {
//        adapter.createRoom();
//
//        Result allRoomssResult = adapter.getRooms();
//        assertThat(status(allRoomssResult)).isEqualTo(OK);
//
//        JSONArray allRoomsJson = new JSONArray(contentAsString(allRoomssResult));
//        assertThat(allRoomsJson.length()).isEqualTo(1);
//    }
//
//    @Test
//    public void showRoomSuccess() throws JSONException {
//        long createdId = adapter.getCreateRoomId();
//
//        Result showResult = adapter.showRoom(createdId);
//        assertThat(status(showResult)).isEqualTo(OK);
//
//        JSONObject showJsonResult = new JSONObject(contentAsString(showResult));
//        assertThat(showJsonResult.getLong(RoomsControllerAdapter.ID_KEY)).isEqualTo(createdId);
//        assertThat(showJsonResult.getString(RoomsControllerAdapter.NAME_KEY)).isEqualTo(RoomsControllerAdapter.NAME);
//        assertThat(showJsonResult.getLong(RoomsControllerAdapter.LAT_KEY) == RoomsControllerAdapter.LAT);
//        assertThat(showJsonResult.getLong(RoomsControllerAdapter.LON_KEY) == RoomsControllerAdapter.LON);
//        assertThat(showJsonResult.getInt(RoomsControllerAdapter.RADIUS_KEY) == RoomsControllerAdapter.RADIUS);
//    }
//
//    @Test
//    public void showRoomBadId() throws JSONException {
//        Result showBadId = adapter.showRoom(1);
//        assertThat(status(showBadId)).isEqualTo(NOT_FOUND);
//    }
//
//    @Test
//    public void updateRoomSuccess() throws JSONException {
//        long createdId = adapter.getCreateRoomId();
//
//        String updateRoomId = "-1";
//        String updateName = "NEW NAME";
//        long updateLat = 99999;
//        long updateLon = 99999;
//        long updateRadius = 99999;
//        Integer updateScore = 5;
//
//        Map<String, String> updateFields = new HashMap<>();
//        updateFields.put(RoomsControllerAdapter.ID_KEY, updateRoomId);
//        updateFields.put(RoomsControllerAdapter.SCORE_KEY, String.valueOf(updateScore));
//        updateFields.put(RoomsControllerAdapter.LAT_KEY, String.valueOf(updateLat));
//        updateFields.put(RoomsControllerAdapter.LON_KEY, String.valueOf(updateLon));
//        updateFields.put(RoomsControllerAdapter.RADIUS_KEY, String.valueOf(updateRadius));
//        updateFields.put(RoomsControllerAdapter.NAME_KEY, updateName);
//
//        Result updateResult = adapter.updateRoom(createdId, updateFields);
//        assertThat(status(updateResult)).isEqualTo(OK);
//
//        JSONObject updateJson = new JSONObject(contentAsString(updateResult));
//        assertThat(updateJson.getLong(RoomsControllerAdapter.ID_KEY)).isEqualTo(createdId);
//        assertThat(updateJson.getInt(RoomsControllerAdapter.SCORE_KEY)).isEqualTo(updateScore);
//        assertThat(updateJson.getLong(RoomsControllerAdapter.LAT_KEY)).isEqualTo(RoomsControllerAdapter.LAT); //check that this was not updated
//        assertThat(updateJson.getLong(RoomsControllerAdapter.LON_KEY)).isEqualTo(RoomsControllerAdapter.LON); //check that this was not updated
//        assertThat(updateJson.getString(RoomsControllerAdapter.NAME_KEY)).isEqualTo(RoomsControllerAdapter.NAME); //check that this was not updated
//
//        Result showResult = adapter.showRoom(createdId);
//        assertThat(status(showResult)).isEqualTo(OK);
//
//        JSONObject showJson = new JSONObject(contentAsString(showResult));
//        assertThat(showJson.getInt(RoomsControllerAdapter.SCORE_KEY)).isEqualTo(updateScore);
//        assertThat(showJson.getString(RoomsControllerAdapter.NAME_KEY)).isEqualTo(RoomsControllerAdapter.NAME);
//        assertThat(showJson.getLong(RoomsControllerAdapter.LAT_KEY)).isEqualTo(RoomsControllerAdapter.LAT);
//        assertThat(showJson.getLong(RoomsControllerAdapter.LON_KEY)).isEqualTo(RoomsControllerAdapter.LON);
//    }
//
//    @Test
//    public void updateRoomBadId() {
//        Result updateBadId = adapter.updateRoom(1, Collections.emptyMap());
//        assertThat(status(updateBadId)).isEqualTo(NOT_FOUND);
//    }
//
//    @Test
//    public void deleteRoomSuccess() throws JSONException {
//        long createId = adapter.getCreateRoomId();
//
//        Result deleteResult = adapter.deleteRoom(createId);
//        assertThat(status(deleteResult)).isEqualTo(OK);
//
//        Result showResult = adapter.showRoom(createId);
//        assertThat(status(showResult)).isEqualTo(NOT_FOUND);
//    }
//
//    @Test
//    public void deleteRoomBadId() {
//        Result deleteBadId = adapter.deleteRoom(1);
//        assertThat(status(deleteBadId)).isEqualTo(NOT_FOUND);
//    }
}
