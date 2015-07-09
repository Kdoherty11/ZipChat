package unit.controllers;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import controllers.PublicRoomsController;
import factories.MessageFactory;
import factories.PublicRoomFactory;
import models.entities.Message;
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
import play.Logger;
import play.mvc.Action;
import play.mvc.Result;
import play.test.FakeRequest;
import play.test.WithApplication;
import services.MessageService;
import services.PublicRoomService;
import services.UserService;
import services.impl.SecurityServiceImpl;
import utils.AbstractResultSender;
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
    private MessageFactory messageFactory;

    @Mock
    private PublicRoomService publicRoomService;

    @Mock
    private MessageService messageService;

    @Mock
    private SecurityServiceImpl securityServiceImpl;

    @Mock
    private UserService userService;

    @Before
    @SuppressWarnings("deprecation")
    public void setUp() throws Exception {
        publicRoomFactory = new PublicRoomFactory();
        messageFactory = new MessageFactory();
        controller = new PublicRoomsController(publicRoomService, messageService,
                securityServiceImpl, userService);

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

    private class CreateResultSender extends AbstractResultSender {

        private Map<String, String> params = new HashMap<>();

        public CreateResultSender() {
            super(POST, "/publicRooms");
        }

        public CreateResultSender setName(String roomName) {
            params.put("name", roomName);
            return this;
        }

        public CreateResultSender setLatitude(String latitude) {
            params.put("latitude", latitude);
            return this;
        }

        public CreateResultSender setLongitude(String longitude) {
            params.put("longitude", longitude);
            return this;
        }

        public CreateResultSender setRadius(String radius) {
            params.put("radius", radius);
            return this;
        }

        @Override
        public Result send() {
            FakeRequest request = getFakeRequest().withFormUrlEncodedBody(params);
            return route(request);
        }
    }

    @Test
    public void createRoomSuccess() throws JSONException, InstantiationException, IllegalAccessException {
        String roomName = "roomName";
        String latitude = "10.0";
        String longitude = "12.0";
        String radius = "5";

        Result createResult = new CreateResultSender()
                .setName(roomName).setLatitude(latitude).setLongitude(longitude).setRadius(radius).send();

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
        Result noNameResult = new CreateResultSender().setLatitude("2.0").setLongitude("4.0").setRadius("1").send();

        assertThat(status(noNameResult)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void createRoomNoLatitude() {
        Result noLatResult = new CreateResultSender().setName("name").setLongitude("4.0").setRadius("1").send();

        assertThat(status(noLatResult)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void createRoomNoLongitude() {
        Result noLonResult = new CreateResultSender().setName("name").setLatitude("4.0").setRadius("1").send();

        assertThat(status(noLonResult)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void createRoomNoRadius() {
        Result noRadiusResult = new CreateResultSender().setName("name").setLatitude("4.0").setLongitude("3.0").send();

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
        when(securityServiceImpl.isUnauthorized(userId)).thenReturn(true);

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
        when(securityServiceImpl.isUnauthorized(userId)).thenReturn(false);

        final Result result = route(new FakeRequest(DELETE, "/publicRooms/123/subscriptions/" + userId));

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(publicRoomService, userService);
    }

    @Test
    public void removeSubscriptionRoomIdMustBePositive() {
        long userId = 1;
        when(securityServiceImpl.isUnauthorized(userId)).thenReturn(false);

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

        when(securityServiceImpl.isUnauthorized(userId)).thenReturn(false);
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

        when(securityServiceImpl.isUnauthorized(userId)).thenReturn(false);
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

        when(securityServiceImpl.isUnauthorized(userId)).thenReturn(false);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(publicRoomService.unsubscribe(mockRoom, mockUser)).thenReturn(false);

        final Result result = route(new FakeRequest(DELETE, "/publicRooms/" + roomId + "/subscriptions/" + userId));

        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void removeSubscriptionUnauthorizedUser() {
        long userId = 1;
        when(securityServiceImpl.isUnauthorized(userId)).thenReturn(true);

        final Result result = route(new FakeRequest(DELETE, "/publicRooms/2/subscriptions/" + userId));

        assertThat(status(result)).isEqualTo(FORBIDDEN);
        verifyZeroInteractions(userService, publicRoomService);
    }

    private class GetMessagesRequestSender extends AbstractResultSender {

        private Integer limit;
        private Integer offset;


        protected GetMessagesRequestSender(long roomId) {
            super(GET, "/publicRooms/" + roomId + "/messages");
        }

        public GetMessagesRequestSender setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public GetMessagesRequestSender setOffset(int offset) {
            this.offset = offset;
            return this;
        }

        @Override
        public Result send() {
            if (limit != null) {
                addQueryParam("limit", limit);
            }

            if (offset != null) {
                addQueryParam("offset", offset);
            }

            Logger.error("URL: " + url);

            return route(getFakeRequest());
        }
    }

    @Test
    public void getMessagesRoomNotFound() {
        long roomId = 1;
        when(publicRoomService.findById(roomId)).thenReturn(Optional.empty());

        final Result result = new GetMessagesRequestSender(roomId).setOffset(0).setLimit(25).send();

        assertThat(status(result)).isEqualTo(NOT_FOUND);
    }

    @Test
    public void getMessagesWithNegativeOffset() {
        long roomId = 1;
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));

        final Result result = new GetMessagesRequestSender(roomId).setOffset(-1).setLimit(25).send();

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(messageService);
    }

    @Test
    public void getMessagesWithNegativeLimit() {
        long roomId = 1;
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));

        final Result result = new GetMessagesRequestSender(roomId).setOffset(0).setLimit(-10).send();

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        verifyZeroInteractions(messageService);
    }

    @Test
    public void getMessagesWorksWithZeroLimitAndZeroOffset() throws IllegalAccessException, InstantiationException {
        long roomId = 1;
        int limit = 0;
        int offset = 0;
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        List<Message> messages = messageFactory.createList(3);
        when(messageService.getMessages(roomId, limit, offset)).thenReturn(messages);

        final Result result = new GetMessagesRequestSender(roomId).setOffset(offset).setLimit(limit).send();

        assertThat(status(result)).isEqualTo(OK);
        Gson gson = new Gson();
        Message[] returnedMessages = gson.fromJson(contentAsString(result), Message[].class);
        assertThat(returnedMessages).hasSize(3);
        for (int i = 0; i < returnedMessages.length; i++) {
            assertThat(returnedMessages[i]).isEqualTo(messages.get(i));
        }
    }

}
