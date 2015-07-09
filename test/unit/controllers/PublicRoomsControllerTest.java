package unit.controllers;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import controllers.MessagesController;
import controllers.PublicRoomsController;
import factories.PublicRoomFactory;
import models.entities.PublicRoom;
import models.entities.User;
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
import utils.TestUtils;

import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
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
        String roomName = "roomName";
        String latitude = "10.0";
        String longitude = "12.0";
        String radius = "5";

        Result createResult = new CreateResultBuilder()
                .setName(roomName).setLatitude(latitude).setLongitude(longitude).setRadius(radius).build();

        assertThat(status(createResult)).isEqualTo(CREATED);
        Gson gson = new Gson();
        PublicRoom room = gson.fromJson(contentAsString(createResult), PublicRoom.class);
        assertThat(room.name).isEqualTo(roomName);
        assertThat(room.latitude).isEqualTo(Double.parseDouble(latitude));
        assertThat(room.longitude).isEqualTo(Double.parseDouble(longitude));
        assertThat(room.radius).isEqualTo(Integer.parseInt(radius));
        verify(publicRoomService).save(eq(room));
    }

    @Test
    public void createRoomNoName() {
        Result noNameResult = new CreateResultBuilder().setLatitude("2.0").setLongitude("4.0").setRadius("1").build();

        assertThat(status(noNameResult)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void createRoomNoLatitude() {
        Result noLatResult = new CreateResultBuilder().setName("name").setLongitude("4.0").setRadius("1").build();

        assertThat(status(noLatResult)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void createRoomNoLongitude() {
        Result noLonResult = new CreateResultBuilder().setName("name").setLatitude("4.0").setRadius("1").build();

        assertThat(status(noLonResult)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void createRoomNoRadius() {
        Result noRadiusResult = new CreateResultBuilder().setName("name").setLatitude("4.0").setLongitude("3.0").build();

        assertThat(status(noRadiusResult)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void getGeoRooms() throws Throwable {
        List<PublicRoom> rooms = publicRoomFactory.createList(3);
        when(publicRoomService.allInGeoRange(anyDouble(), anyDouble())).thenReturn(rooms);

        final Result result = route(fakeRequest(GET, "/publicRooms?lat=2.0&lon=43.0"));

        assertThat(status(result)).isEqualTo(OK);
        JSONArray resultRooms = TestUtils.parseJsonArray(result);
        assertThat(resultRooms.length()).isEqualTo(3);
        new JsonArrayIterator(resultRooms).forEach(JsonValidator::validatePublicRoom);
    }

    @Test
    public void getGeoRoomsEmpty() throws JSONException {
        List<PublicRoom> rooms = new ArrayList<>();
        when(publicRoomService.allInGeoRange(anyDouble(), anyDouble())).thenReturn(rooms);

        final Result result = route(fakeRequest(GET, "/publicRooms?lat=2.0&lon=43.0"));

        assertThat(status(result)).isEqualTo(OK);
        JSONArray resultRooms = TestUtils.parseJsonArray(result);
        assertThat(resultRooms.length()).isZero();
    }

    @Test
    public void getGeoRoomsNoLat() {
        final Result result = route(fakeRequest(GET, "/publicRooms?lon=43.0"));
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void getGeoRoomsNoLon() {
        final Result result = route(fakeRequest(GET, "/publicRooms?lat=43.0"));
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void geoRoomsNoParams() {
        final Result result = route(fakeRequest(GET, "/publicRooms"));
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createSubscription() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(publicRoomService.subscribe(mockRoom, mockUser)).thenReturn(true);

        FakeRequest request = new FakeRequest(POST, "/publicRooms/" + roomId + "/subscriptions").withFormUrlEncodedBody(ImmutableMap.of("userId", Long.toString(userId)));
        final Result result = route(request);
        assertThat(status(result)).isEqualTo(CREATED);
    }

    @Test
    public void createSubscriptionNoUserId() {
        final Result result = route(new FakeRequest(POST, "/publicRooms/1/subscriptions"));

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(userService, publicRoomService);
    }

    @Test
    public void createSubscriptionUserIdIsNotALong() {
        FakeRequest fakeRequest = new FakeRequest(POST, "/publicRooms/1/subscriptions").withFormUrlEncodedBody(ImmutableMap.of("userId", "notALong"));

        final Result result = route(fakeRequest);

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(userService, publicRoomService);
    }

    @Test
    public void createSubscriptionUserMustBePositive() {
        FakeRequest fakeRequest = new FakeRequest(POST, "/publicRooms/1/subscriptions").withFormUrlEncodedBody(ImmutableMap.of("userId", "-1"));

        final Result result = route(fakeRequest);

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(userService, publicRoomService);
    }

    @Test
    public void createSubscriptionRoomDoesNotExist() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(publicRoomService.findById(roomId)).thenReturn(Optional.empty());
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(publicRoomService.subscribe(mockRoom, mockUser)).thenReturn(true);

        FakeRequest request = new FakeRequest(POST, "/publicRooms/" + roomId + "/subscriptions").withFormUrlEncodedBody(ImmutableMap.of("userId", Long.toString(userId)));
        final Result result = route(request);
        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void createSubscriptionUserDoesNotExist() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(userService.findById(userId)).thenReturn(Optional.empty());
        when(publicRoomService.subscribe(mockRoom, mockUser)).thenReturn(true);

        FakeRequest request = new FakeRequest(POST, "/publicRooms/" + roomId + "/subscriptions").withFormUrlEncodedBody(ImmutableMap.of("userId", Long.toString(userId)));
        final Result result = route(request);
        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void createSubscriptionUserIsAlreadySubscribed() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(publicRoomService.subscribe(mockRoom, mockUser)).thenReturn(false);

        FakeRequest request = new FakeRequest(POST, "/publicRooms/" + roomId + "/subscriptions").withFormUrlEncodedBody(ImmutableMap.of("userId", Long.toString(userId)));
        final Result result = route(request);
        assertThat(status(result)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createSubscriptionUnauthorizedUser() {
        long userId = 1;
        when(securityHelper.isUnauthorized(userId)).thenReturn(true);

        FakeRequest request = new FakeRequest(POST, "/publicRooms/1/subscriptions").withFormUrlEncodedBody(ImmutableMap.of("userId", Long.toString(userId)));
        final Result result = route(request);

        assertThat(status(result)).isEqualTo(FORBIDDEN);
        verifyZeroInteractions(userService, publicRoomService);
    }

    @Test
    public void removeSubscription() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(publicRoomService.unsubscribe(mockRoom, mockUser)).thenReturn(true);

        final Result result = route(new FakeRequest(DELETE, "/publicRooms/" + roomId + "/subscriptions/" + userId));
        assertThat(status(result)).isEqualTo(OK);
    }

    @Test
    public void removeSubscriptionUserIdIsNotALong() {
        final Result result = route(new FakeRequest(DELETE, "/publicRooms/123/subscriptions/notALong"));

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(userService, publicRoomService);
    }

    @Test
    public void removeSubscriptionUserIdMustBePositive() {
        long userId = -1;
        when(securityHelper.isUnauthorized(userId)).thenReturn(false);

        final Result result = route(new FakeRequest(DELETE, "/publicRooms/123/subscriptions/" + userId));

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(publicRoomService, userService);
    }

    @Test
    public void removeSubscriptionRoomIdMustBePositive() {
        long userId = 1;
        when(securityHelper.isUnauthorized(userId)).thenReturn(false);

        final Result result = route(new FakeRequest(DELETE, "/publicRooms/-1/subscriptions/" + userId));

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(publicRoomService, userService);
    }

    @Test
    public void removeSubscriptionRoomDoesNotExist() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(securityHelper.isUnauthorized(userId)).thenReturn(false);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.empty());
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(publicRoomService.unsubscribe(mockRoom, mockUser)).thenReturn(true);

        final Result result = route(new FakeRequest(DELETE, "/publicRooms/" + roomId + "/subscriptions/" + userId));

        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void removeSubscriptionUserDoesNotExist() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(securityHelper.isUnauthorized(userId)).thenReturn(false);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(userService.findById(userId)).thenReturn(Optional.empty());
        when(publicRoomService.unsubscribe(mockRoom, mockUser)).thenReturn(true);

        final Result result = route(new FakeRequest(DELETE, "/publicRooms/" + roomId + "/subscriptions/" + userId));

        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void removeSubscriptionUserIsNotSubscribed() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(securityHelper.isUnauthorized(userId)).thenReturn(false);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(publicRoomService.unsubscribe(mockRoom, mockUser)).thenReturn(false);

        final Result result = route(new FakeRequest(DELETE, "/publicRooms/" + roomId + "/subscriptions/" + userId));

        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void removeSubscriptionUnauthorizedUser() {
        long userId = 1;
        when(securityHelper.isUnauthorized(userId)).thenReturn(true);

        final Result result = route(new FakeRequest(DELETE, "/publicRooms/2/subscriptions/" + userId));

        assertThat(status(result)).isEqualTo(FORBIDDEN);
        verifyZeroInteractions(userService, publicRoomService);
    }

}
