package unit.controllers;

import controllers.MessagesController;
import controllers.PublicRoomsController;
import factories.PublicRoomFactory;
import models.entities.PublicRoom;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.GlobalSettings;
import play.mvc.Action;
import play.mvc.Result;
import play.test.FakeRequest;
import play.test.WithApplication;
import security.SecurityHelper;
import services.PublicRoomService;
import services.UserService;
import utils.AbstractResultBuilder;
import utils.JsonArrayIterator;
import utils.JsonValidator;
import utils.ResultValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.when;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
public class PublicRoomsControllerTest extends WithApplication {

    private PublicRoomsController controller;
    private PublicRoomFactory publicRoomFactory;

    @Mock
    private PublicRoomService publicRoomService;

    @Mock
    private MessagesController messagesController;

    @Mock
    private SecurityHelper securityHelper;

    @Mock
    private UserService userService;

    @Before
    @SuppressWarnings("deprecation")
    public void setUp() throws Exception {
        publicRoomFactory = new PublicRoomFactory();
        controller = new PublicRoomsController(publicRoomService, messagesController,
                securityHelper, userService);

        final GlobalSettings global = new GlobalSettings() {

            @Override
            @SuppressWarnings("unchecked")
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
    public void getGeoRoomsEmpty() throws JSONException {
        List<PublicRoom> rooms = new ArrayList<>();
        when(publicRoomService.allInGeoRange(anyDouble(), anyDouble())).thenReturn(rooms);
        final Result result = route(fakeRequest(GET, "/publicRooms?lat=2.0&lon=43.0"));
        assertThat(status(result)).isEqualTo(OK);
        JSONArray resultRooms = new JSONArray(contentAsString(result));
        assertThat(resultRooms.length()).isZero();
    }

    @Test
    public void getGeoRooms() throws Throwable {
        List<PublicRoom> rooms = publicRoomFactory.createList(3);
        when(publicRoomService.allInGeoRange(anyDouble(), anyDouble())).thenReturn(rooms);
        final Result result = route(fakeRequest(GET, "/publicRooms?lat=2.0&lon=43.0"));

        assertThat(status(result)).isEqualTo(OK);
        JSONArray resultRooms = new JSONArray(contentAsString(result));
        assertThat(resultRooms.length()).isEqualTo(3);
        new JsonArrayIterator(resultRooms).forEach(JsonValidator::validatePublicRoom);
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

    private class CreateResultBuilder extends AbstractResultBuilder {

        private Map<String, String> params = new HashMap<>();

        public CreateResultBuilder() {
            super(POST, "/publicRooms");
        }

        public CreateResultBuilder setName(String roomName) {
            params.put("name", roomName);
            return this;
        }

        public CreateResultBuilder setLatitude(String latitude) {
            params.put("latitude", latitude);
            return this;
        }

        public CreateResultBuilder setLongitude(String longitude) {
            params.put("longitude", longitude);
            return this;
        }

        public CreateResultBuilder setRadius(String radius) {
            params.put("radius", radius);
            return this;
        }

        public Result build() {
            FakeRequest request = buildFakeRequest().withFormUrlEncodedBody(params);
            return route(request);
        }
    }

    @Test
    public void createRoomSuccess() throws JSONException, InstantiationException, IllegalAccessException {
        PublicRoom createdRoom = publicRoomFactory.create();
        //when(publicRoomService.save(any(PublicRoom.class))) then set its Id
        Result createResult = new CreateResultBuilder()
                .setName("roomName").setLatitude("10.0").setLongitude("12.0").setRadius("5").build();
        ResultValidator.validateCreateResult(createResult, "roomId");
    }




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

}
