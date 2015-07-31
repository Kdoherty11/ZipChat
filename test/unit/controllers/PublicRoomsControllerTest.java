package unit.controllers;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import controllers.PublicRoomsController;
import factories.MessageFactory;
import factories.PublicRoomFactory;
import models.entities.Message;
import models.entities.PublicRoom;
import models.entities.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import services.MessageService;
import services.PublicRoomService;
import services.SecurityService;
import services.UserService;
import utils.AbstractRequestSender;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
public class PublicRoomsControllerTest extends WithApplication {

    private PublicRoomsController controller;
    private PublicRoomFactory publicRoomFactory;
    private Gson gson;
    private Helpers helpers;

    @Mock
    private PublicRoomService publicRoomService;

    @Mock
    private MessageService messageService;

    @Mock
    private SecurityService securityService;

    @Mock
    private UserService userService;

    @Before
    public void setUp() {
        publicRoomFactory = new PublicRoomFactory();
        gson = new Gson();
        helpers = new Helpers();
        controller = new PublicRoomsController(publicRoomService, messageService,
                securityService, userService);

        start(fakeApplication());
    }

    private class CreateRequestSender extends AbstractRequestSender {

        private Map<String, String> params = new HashMap<>();

        public CreateRequestSender() {
            super(POST, "/publicRooms");
        }

        public CreateRequestSender setName(String roomName) {
            params.put("name", roomName);
            return this;
        }

        public CreateRequestSender setLatitude(String latitude) {
            params.put("latitude", latitude);
            return this;
        }

        public CreateRequestSender setLongitude(String longitude) {
            params.put("longitude", longitude);
            return this;
        }

        public CreateRequestSender setRadius(String radius) {
            params.put("radius", radius);
            return this;
        }

        @Override
        public Result send() {
            RequestBuilder requestBuilder = getRequestBuilder();
            if (!params.isEmpty()) {
                requestBuilder.bodyForm(params);
            }
            return helpers.invokeWithContext(requestBuilder, controller::createRoom);
        }
    }

    @Test
    public void createRoomSuccess() {
        Double latitude = 10.0;
        Double longitude = 12.0;
        Integer radius = 5;

        String roomName = "roomName";
        String latitudeStr = latitude.toString();
        String longitudeStr = longitude.toString();
        String radiusStr = radius.toString();

        Result createResult = new CreateRequestSender()
                .setName(roomName).setLatitude(latitudeStr).setLongitude(longitudeStr).setRadius(radiusStr).send();


        assertEquals(CREATED, createResult.status());
        Gson gson = new Gson();
        PublicRoom room = gson.fromJson(contentAsString(createResult), PublicRoom.class);
        assertEquals(roomName, room.name);
        assertEquals(latitude, room.latitude);
        assertEquals(longitude, room.longitude);
        assertEquals(radius, room.radius);
        verify(publicRoomService).save(any(PublicRoom.class));
    }

    @Test
    public void createRoomNoName() {
        Result noNameResult = new CreateRequestSender().setLatitude("2.0").setLongitude("4.0").setRadius("1").send();

        assertEquals(BAD_REQUEST, noNameResult.status());
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void createRoomNoLatitude() {
        Result noLatResult = new CreateRequestSender().setName("name").setLongitude("4.0").setRadius("1").send();

        assertEquals(BAD_REQUEST, noLatResult.status());
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void createRoomNoLongitude() {
        Result noLonResult = new CreateRequestSender().setName("name").setLatitude("4.0").setRadius("1").send();

        assertEquals(BAD_REQUEST, noLonResult.status());
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void createRoomNoRadius() {
        Result noRadiusResult = new CreateRequestSender().setName("name").setLatitude("4.0").setLongitude("3.0").send();

        assertEquals(BAD_REQUEST, noRadiusResult.status());
        verifyZeroInteractions(publicRoomService);
    }

    @Test
    public void getGeoRooms() throws Throwable {
        int numRooms = 3;
        List<PublicRoom> rooms = publicRoomFactory.createList(numRooms);
        when(publicRoomService.allInGeoRange(anyDouble(), anyDouble())).thenReturn(rooms);

        final Result result = controller.getGeoRooms(2.0, 43.0);
        PublicRoom[] publicRooms = gson.fromJson(contentAsString(result), PublicRoom[].class);

        assertEquals(OK, result.status());
        assertEquals(numRooms, publicRooms.length);
    }

    @Test
    public void getGeoRoomsEmpty() {
        List<PublicRoom> rooms = new ArrayList<>();
        when(publicRoomService.allInGeoRange(anyDouble(), anyDouble())).thenReturn(rooms);

        final Result result = controller.getGeoRooms(2.0, 43.0);
        PublicRoom[] publicRooms = gson.fromJson(contentAsString(result), PublicRoom[].class);

        assertEquals(OK, result.status());
        assertEquals(0, publicRooms.length);
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


        RequestBuilder requestBuilder = new RequestBuilder().bodyForm(ImmutableMap.of("userId", Long.toString(userId)));
        final Result result = new Helpers().invokeWithContext(requestBuilder, () -> controller.createSubscription(roomId));

        assertEquals(CREATED, result.status());
    }

    @Test
    public void createSubscriptionNoUserId() {
        final Result result = route(new RequestBuilder().method(POST).uri("/publicRooms/1/subscriptions"));

        assertEquals(BAD_REQUEST, result.status());
        verifyZeroInteractions(userService, publicRoomService);
    }

    @Test
    public void createSubscriptionUserIdIsNotALong() {
        RequestBuilder requestBuilder = new RequestBuilder().method(POST).uri("/publicRooms/1/subscriptions").bodyForm(ImmutableMap.of("userId", "notALong"));
        final Result result = route(requestBuilder);

        assertEquals(BAD_REQUEST, result.status());
        verifyZeroInteractions(userService, publicRoomService);
    }

    @Test
    public void createSubscriptionUserMustBePositive() {
        RequestBuilder requestBuilder = new RequestBuilder().method(POST).uri("/publicRooms/1/subscriptions").bodyForm(ImmutableMap.of("userId", "-1"));

        final Result result = route(requestBuilder);

        assertEquals(BAD_REQUEST, result.status());
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

        RequestBuilder requestBuilder = new RequestBuilder().method(POST).uri("/publicRooms/" + roomId + "/subscriptions").bodyForm(ImmutableMap.of("userId", Long.toString(userId)));
        final Result result = route(requestBuilder);

        assertEquals(NOT_FOUND, result.status());
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

        RequestBuilder requestBuilder = new RequestBuilder().method(POST).uri("/publicRooms/" + roomId + "/subscriptions").bodyForm(ImmutableMap.of("userId", Long.toString(userId)));
        final Result result = route(requestBuilder);

        assertEquals(NOT_FOUND, result.status());
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

        RequestBuilder requestBuilder = new RequestBuilder().bodyForm(ImmutableMap.of("userId", Long.toString(userId)));
        final Result result = helpers.invokeWithContext(requestBuilder, () -> controller.createSubscription(roomId));

        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void createSubscriptionUnauthorizedUser() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(true);

        RequestBuilder requestBuilder = new RequestBuilder().bodyForm(ImmutableMap.of("userId", Long.toString(userId)));
        final Result result = helpers.invokeWithContext(requestBuilder, () -> controller.createSubscription(1));

        assertEquals(FORBIDDEN, result.status());
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

        final Result result = controller.removeSubscription(roomId, userId);

        assertEquals(OK, result.status());
    }

    @Test
    public void removeSubscriptionUserIdIsNotALong() {
        final Result result = route(new RequestBuilder().method(DELETE).uri("/publicRooms/123/subscriptions/notALong"));

        assertEquals(BAD_REQUEST, result.status());
        verifyZeroInteractions(userService, publicRoomService);
    }

    @Test
    public void removeSubscriptionUserIdMustBePositive() {
        long userId = -1;
        when(securityService.isUnauthorized(userId)).thenReturn(false);

        final Result result = route(new RequestBuilder().method(DELETE).uri("/publicRooms/123/subscriptions/" + userId));

        assertEquals(BAD_REQUEST, result.status());
        verifyZeroInteractions(publicRoomService, userService);
    }

    @Test
    public void removeSubscriptionRoomIdMustBePositive() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(false);

        final Result result = route(new RequestBuilder().method(DELETE).uri("/publicRooms/-1/subscriptions/" + userId));

        assertEquals(BAD_REQUEST, result.status());
        verifyZeroInteractions(publicRoomService, userService);
    }

    @Test
    public void removeSubscriptionRoomDoesNotExist() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.empty());
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(publicRoomService.unsubscribe(mockRoom, mockUser)).thenReturn(true);

        final Result result = route(new RequestBuilder().method(DELETE).uri("/publicRooms/" + roomId + "/subscriptions/" + userId));

        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void removeSubscriptionUserDoesNotExist() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(userService.findById(userId)).thenReturn(Optional.empty());
        when(publicRoomService.unsubscribe(mockRoom, mockUser)).thenReturn(true);

        final Result result = route(new RequestBuilder().method(DELETE).uri("/publicRooms/" + roomId + "/subscriptions/" + userId));

        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void removeSubscriptionUserIsNotSubscribed() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);
        User mockUser = mock(User.class);

        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(userService.findById(userId)).thenReturn(Optional.of(mockUser));
        when(publicRoomService.unsubscribe(mockRoom, mockUser)).thenReturn(false);

        final Result result = controller.removeSubscription(roomId, userId);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains("not subscribed"));
    }

    @Test
    public void removeSubscriptionUserNotFound() {
        long roomId = 1;
        long userId = 2;
        PublicRoom mockRoom = mock(PublicRoom.class);

        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        when(userService.findById(userId)).thenReturn(Optional.empty());

        final Result result = controller.removeSubscription(roomId, userId);

        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void removeSubscriptionUnauthorizedUser() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(true);

        final Result result = controller.removeSubscription(2, userId);

        assertEquals(FORBIDDEN, result.status());
        verifyZeroInteractions(userService, publicRoomService);
    }

    @Test
    public void getMessagesRoomNotFound() {
        long roomId = 1;
        when(publicRoomService.findById(roomId)).thenReturn(Optional.empty());

        Result result = controller.getMessages(roomId, 1, 1);

        assertEquals(NOT_FOUND, result.status());
        verifyZeroInteractions(messageService);
    }

    @Test
    public void getMessagesWithNegativeOffset() {
        long roomId = 1;
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));

        final Result result = controller.getMessages(roomId, 1, -1);

        assertEquals(BAD_REQUEST, result.status());
        verifyZeroInteractions(messageService);
    }

    @Test
    public void getMessagesWithNegativeLimit() {
        long roomId = 1;
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));

        final Result result = controller.getMessages(roomId, -1, 1);

        assertEquals(BAD_REQUEST, result.status());
        verifyZeroInteractions(messageService);
    }

    @Test
    public void getMessagesWorksWithZeroLimitAndZeroOffset() throws IllegalAccessException, InstantiationException {
        long roomId = 1;
        int limit = 0;
        int offset = 0;
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(mockRoom));
        MessageFactory messageFactory = new MessageFactory();
        List<Message> messages = messageFactory.createList(3);
        when(messageService.getMessages(roomId, limit, offset)).thenReturn(messages);

        Result result = controller.getMessages(roomId, limit, offset);

        assertEquals(OK, result.status());
        Message[] returnedMessages = gson.fromJson(contentAsString(result), Message[].class);
        assertEquals(3, returnedMessages.length);
        for (int i = 0; i < returnedMessages.length; i++) {
            assertEquals(returnedMessages[i], messages.get(i));
        }
    }

}
