package unit.sockets;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.RoomSocketsController;
import factories.*;
import models.*;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.api.Play;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.test.WithApplication;
import redis.clients.jedis.Jedis;
import services.*;
import services.impl.RoomSocketServiceImpl;
import sockets.RoomSocket;
import utils.TestUtils;

import javax.inject.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.libs.Json.fromJson;
import static play.libs.Json.toJson;

/**
 * Created by kdoherty on 8/3/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class RoomSocketTest extends WithApplication {

    private RoomSocketsController roomSocketsController;

    private MockWebSocket webSocket;

    @Mock
    private AbstractRoomService abstractRoomService;

    @Mock
    private AnonUserService anonUserService;

    @Mock
    private MessageService messageService;

    @Mock
    private PublicRoomService publicRoomService;

    @Mock
    private UserService userService;

    @Mock
    private PrivateRoomService privateRoomService;

    private PublicRoom publicRoom;

    private MockJedisService jedisService;

    private Jedis jedis;

    private RoomSocketService roomSocketService;

    private KeepAliveService keepAliveService;

    private final long roomId = 1;

    private final long userId = 2;

    private User user;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static boolean firstTest = true;

    private JsonNode firstEvent;

    private JsonNode secondEvent;

    private UserFactory userFactory;


    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(JedisService.class).to(MockJedisService.class))
                .overrides(bind(UserService.class).to((Provider<UserService>) () -> userService))
                .build();
    }

    @Before
    public void setUp() throws Exception {
        MockJedis.clean();
        ((Map) TestUtils.getHiddenField(RoomSocket.class, "rooms")).clear();

        userFactory = new UserFactory();
        user = userFactory.create(PropOverride.of("userId", userId));
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        publicRoom = new PublicRoomFactory().create(PropOverride.of("roomId", roomId));
        when(abstractRoomService.findById(roomId)).thenReturn(Optional.of(publicRoom));
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(publicRoom));

        jedis = spy(new MockJedis());
        jedisService = new MockJedisService(jedis);

        keepAliveService = spy(Play.current().injector().instanceOf(KeepAliveService.class));
        ActorRef defaultRoomWithMockedServices = Play.current().actorSystem().actorOf(Props.create(RoomSocket.class, () -> {
            return spy(new RoomSocket(abstractRoomService, userService, anonUserService, messageService, jedisService, keepAliveService));
        }));

        TestUtils.setPrivateStaticFinalField(RoomSocket.class, "defaultRoom", defaultRoomWithMockedServices);

        roomSocketService = new RoomSocketServiceImpl(abstractRoomService, publicRoomService, userService, jedisService);
        SecurityService securityService = Play.current().injector().instanceOf(SecurityService.class);
        roomSocketsController = new RoomSocketsController(roomSocketService, privateRoomService, securityService);

        // Hack to do mock static initialization block from RoomSocket
        if (((MockJedis) jedis).getSubscribers(RoomSocket.CHANNEL).isEmpty() && !firstTest) {
            jedis.subscribe(new RoomSocket.MessageListener(), RoomSocket.CHANNEL);
        }

        firstTest = false;

        webSocket = new MockWebSocket(roomSocketsController.joinPublicRoom(roomId, userId, ""));

        firstEvent = webSocket.read();
        secondEvent = webSocket.read();
    }

    @Test
    public void joinSendsRosterNotify() throws Exception {
        JsonNode rosterNotification;
        JsonNode joinSuccess;

        boolean event1IsRosterNotification = RoomSocket.Join.TYPE.equals(firstEvent.get("event").asText());
        boolean event1IsJoinSuccess = "joinSuccess".equals(firstEvent.get("event").asText());
        boolean event2IsRosterNotification = RoomSocket.Join.TYPE.equals(secondEvent.get("event").asText());
        boolean event2IsJoinSuccess = "joinSuccess".equals(secondEvent.get("event").asText());

        if (event1IsRosterNotification && event2IsJoinSuccess) {
            rosterNotification = firstEvent;
            joinSuccess = secondEvent;
        } else if (event1IsJoinSuccess && event2IsRosterNotification) {
            rosterNotification = secondEvent;
            joinSuccess = firstEvent;
        } else {
            throw new AssertionError("Unexpected event is being sent to the socket");
        }

        assertEquals("has entered the room", rosterNotification.get("message").asText());
        assertEquals(user, objectMapper.convertValue(rosterNotification.get("user"), User.class));

        JsonNode message = joinSuccess.get(RoomSocket.MESSAGE_KEY);
        User[] roomMembers = objectMapper.readValue(message.get("roomMembers").toString(), User[].class);
        assertEquals(0, roomMembers.length);
        assertFalse(message.get("isSubscribed").asBoolean());
    }

    @Test
    public void joinAddsUserToJedis() throws Exception {
        assertTrue(jedis.smembers(Long.toString(roomId)).contains(Long.toString(userId)));
    }

    @Test
    public void joinMemberAlreadyInRoomDoesNotAddUserToJedis() {
        roomSocketsController.joinPublicRoom(roomId, userId, "");
        verify(jedis, times(1)).sadd(Long.toString(roomId), Long.toString(userId));
    }

    @Test
    public void joinSendsRosterNotifyToRedis() {
        RoomSocket.RosterNotification rosterNotify = new RoomSocket.RosterNotification(roomId, userId, RoomSocket.Join.TYPE);
        verify(jedis).publish(RoomSocket.CHANNEL, Json.stringify(toJson(rosterNotify)));
    }

    @Test
    public void joinAddsKeepAlive() throws NoSuchFieldException, IllegalAccessException {
        //assertTrue(keepAliveService.hasKeepAlive(roomId));
        verify(keepAliveService).start(roomId);
    }

    private JsonNode sendMessage(MockWebSocket socket, String message, boolean isAnon) throws Throwable {
        JsonNode messageJson = Json.newObject()
                .put("event", RoomSocket.Talk.TYPE)
                .put("message", message)
                .put("isAnon", isAnon);
        socket.write(messageJson);
        return messageJson;
    }

    @Test
    public void talkStoresMessage() throws Throwable {
        String message = "Hello";
        sendMessage(webSocket, message, false);

        // block till we know we got a response
        webSocket.read();

        Set<Long> userIdsInRoom = Collections.singleton(userId);
        verify(abstractRoomService).addMessage(eq(publicRoom), argThat(new TypeSafeMatcher<Message>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Message does not match");
            }

            @Override
            protected boolean matchesSafely(Message msg) {
                return publicRoom.equals(msg.room)
                        && user.equals(msg.sender)
                        && message.equals(msg.message);
            }
        }), eq(userIdsInRoom));
    }

    @Test
    public void talkEventIsSentOut() throws Throwable {
        String message = "Hello";
        sendMessage(webSocket, message, false);

        JsonNode talkEvent = webSocket.read();

        assertEquals(RoomSocket.Talk.TYPE, talkEvent.get("event").asText());
        JsonNode messageNode = Json.parse(talkEvent.get("message").asText());
        assertTrue(messageNode.get("messageId").canConvertToLong());
        assertEquals(user, fromJson(messageNode.get("sender"), User.class));
    }

    @Test
    public void anonTalksAreStoredWithAnonSenders() throws Throwable {
        AnonUser anonUser = new AnonUserFactory().create(PropOverride.of("actual", user));
        when(anonUserService.getOrCreateAnonUser(any(), any())).thenReturn(anonUser);

        String message = "Hello";
        sendMessage(webSocket, message, true);

        // block till we know we got a response
        webSocket.read();

        Set<Long> userIdsInRoom = Collections.singleton(userId);
        verify(abstractRoomService).addMessage(eq(publicRoom), argThat(new TypeSafeMatcher<Message>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Custom message matcher");
            }

            @Override
            protected boolean matchesSafely(Message msg) {
                return publicRoom.equals(msg.room)
                        && anonUser.equals(msg.sender)
                        && message.equals(msg.message);
            }
        }), eq(userIdsInRoom));
    }

    @Test
    public void keepAliveMessagesAreNotStored() throws InterruptedException {
        RoomSocket.remoteMessage(new RoomSocket.Talk(roomId, KeepAliveService.ID, KeepAliveService.MSG));
        webSocket.read();
        verify(abstractRoomService, never()).addMessage(any(), any(), any());
    }

    @Test
    public void keepAliveMessagesFormat() throws InterruptedException {
        RoomSocket.remoteMessage(new RoomSocket.Talk(roomId, KeepAliveService.ID, KeepAliveService.MSG));
        JsonNode keepAliveEvent = webSocket.read();
        assertEquals(RoomSocket.Talk.TYPE, keepAliveEvent.get("event").asText());
        assertEquals(KeepAliveService.MSG, keepAliveEvent.get("message").asText());
    }

    private JsonNode sendFavoriteEvent(MockWebSocket ws, long messageId, RoomSocket.FavoriteNotification.Action action) throws Throwable {
        JsonNode favoriteEvent = Json.newObject()
                .put("event", RoomSocket.FavoriteNotification.TYPE)
                .put("messageId", messageId)
                .put("action", action.name());

        ws.write(favoriteEvent);

        return favoriteEvent;
    }

    @Test
    public void favoriteAMessage() throws Throwable {
        long messageId = 5;
        Message message = new MessageFactory().create(PropOverride.of("room", publicRoom));
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(messageService.favorite(message, user)).thenReturn(true);
        RoomSocket.FavoriteNotification.Action action = RoomSocket.FavoriteNotification.Action.ADD;

        sendFavoriteEvent(webSocket, messageId, action);

        JsonNode favoriteEvent = webSocket.read();
        assertEquals(action.getType(), favoriteEvent.get(RoomSocket.EVENT_KEY).asText());
        assertEquals(messageId, favoriteEvent.get(RoomSocket.MESSAGE_KEY).asLong());
        assertEquals(user, fromJson(favoriteEvent.get(RoomSocket.USER_KEY), User.class));
    }

    @Test
    public void favoriteAMessageThatHasAlreadyBeenFavoritedByThatUser() throws Throwable {
        long messageId = 5;
        Message message = new MessageFactory().create(PropOverride.of("room", publicRoom));
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(messageService.favorite(message, user)).thenReturn(false);
        RoomSocket.FavoriteNotification.Action action = RoomSocket.FavoriteNotification.Action.ADD;

        sendFavoriteEvent(webSocket, messageId, action);

        JsonNode errorEvent = webSocket.read();
        assertEquals("error", errorEvent.get(RoomSocket.EVENT_KEY).asText());
        assertEquals("Problem " + action + "ing a favorite", errorEvent.get(RoomSocket.MESSAGE_KEY).asText());
    }


    @Test
    public void removeFavoriteFromAMessage() throws Throwable {
        long messageId = 5;
        Message message = new MessageFactory().create(PropOverride.of("room", publicRoom));
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(messageService.removeFavorite(message, user)).thenReturn(true);
        RoomSocket.FavoriteNotification.Action action = RoomSocket.FavoriteNotification.Action.REMOVE;

        sendFavoriteEvent(webSocket, messageId, action);

        JsonNode favoriteEvent = webSocket.read();
        assertEquals(action.getType(), favoriteEvent.get(RoomSocket.EVENT_KEY).asText());
        assertEquals(messageId, favoriteEvent.get(RoomSocket.MESSAGE_KEY).asLong());
        assertEquals(user, fromJson(favoriteEvent.get(RoomSocket.USER_KEY), User.class));
    }

    @Test
    public void removeFavoriteFromAMessageHasNotFavorited() throws Throwable {
        long messageId = 5;
        Message message = new MessageFactory().create(PropOverride.of("room", publicRoom));
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(messageService.removeFavorite(message, user)).thenReturn(false);
        RoomSocket.FavoriteNotification.Action action = RoomSocket.FavoriteNotification.Action.REMOVE;

        sendFavoriteEvent(webSocket, messageId, action);

        JsonNode errorEvent = webSocket.read();
        assertEquals("error", errorEvent.get(RoomSocket.EVENT_KEY).asText());
        assertEquals("Problem " + action + "ing a favorite", errorEvent.get(RoomSocket.MESSAGE_KEY).asText());
    }

    @Test
    public void quitRemovesUserFromJedis() throws Throwable {
        long otherUserId = 6;
        User otherUser = userFactory.create(PropOverride.of("userId", otherUserId));
        when(userService.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        MockWebSocket otherSocket = new MockWebSocket(roomSocketsController.joinPublicRoom(roomId, otherUserId, ""));
        // Join roster notification and joinSuccess events
        otherSocket.read();
        otherSocket.read();

        webSocket.close();

        // wait for quit event
        otherSocket.read();
        assertFalse(jedis.smembers(Long.toString(roomId)).contains(Long.toString(userId)));
    }

    @Test
    public void quitSendsQuitEvent() throws Throwable {
        long otherUserId = 6;
        User otherUser = userFactory.create(PropOverride.of("userId", otherUserId));
        when(userService.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        MockWebSocket otherSocket = new MockWebSocket(roomSocketsController.joinPublicRoom(roomId, otherUserId, ""));
        // Join roster notification and joinSuccess events
        otherSocket.read();
        otherSocket.read();

        webSocket.close();

        JsonNode quitEvent = otherSocket.read();
        assertEquals(RoomSocket.Quit.TYPE, quitEvent.get(RoomSocket.EVENT_KEY).asText());
        assertEquals("has left the room", quitEvent.get(RoomSocket.MESSAGE_KEY).asText());
        assertEquals(user, fromJson(quitEvent.get(RoomSocket.USER_KEY), User.class));
    }

    @Test
    public void quitRemovesKeepAliveIfLastUser() throws Throwable {
        webSocket.close();
        webSocket.read();

        verify(keepAliveService).stop(eq(roomId));
    }

    @Test
    public void quitDoesNotRemoveKeepAliveIfNotLastUser() throws Throwable {
        long otherUserId = 6;
        User otherUser = userFactory.create(PropOverride.of("userId", otherUserId));
        when(userService.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        MockWebSocket otherSocket = new MockWebSocket(roomSocketsController.joinPublicRoom(roomId, otherUserId, ""));
        // Join roster notification and joinSuccess events
        otherSocket.read();
        otherSocket.read();

        webSocket.close();

        otherSocket.read();

        verify(keepAliveService, never()).stop(eq(roomId));
    }

    @Test
    public void quitsCanBeReceivedThroughJedis() throws InterruptedException, InstantiationException, IllegalAccessException {
        long otherUserId = 6;
        User otherUser = userFactory.create(PropOverride.of("userId", otherUserId));
        when(userService.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        MockWebSocket otherSocket = new MockWebSocket(roomSocketsController.joinPublicRoom(roomId, otherUserId, ""));
        // Join roster notification and joinSuccess events
        otherSocket.read();
        otherSocket.read();

        JsonNode quit = Json.newObject()
                .put("type", RoomSocket.Quit.TYPE)
                .put("roomId", roomId)
                .put("userId", userId);
        jedis.publish(RoomSocket.CHANNEL, Json.stringify(quit));

        JsonNode quitEvent = otherSocket.read();
        assertEquals(RoomSocket.Quit.TYPE, quitEvent.get(RoomSocket.EVENT_KEY).asText());
        assertEquals("has left the room", quitEvent.get(RoomSocket.MESSAGE_KEY).asText());
        assertEquals(user, fromJson(quitEvent.get(RoomSocket.USER_KEY), User.class));
    }

    @Test(expected = RuntimeException.class)
    public void unsupportedSocketEvent() throws Throwable {
        webSocket.write(Json.newObject().put("event", "unsupportedEvent"));
    }

    @Test
    public void anonMessageNotAllowedInPrivateRooms() throws Throwable {
//        when(abstractRoomService.findById(roomId)).thenReturn(Optional.of(new PrivateRoomFactory().create()));
//        sendMessage(webSocket, "msg", true);
    }
}
