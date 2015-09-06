package unit.sockets;

import actors.RoomActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.RoomSocketsController;
import factories.*;
import models.AnonUser;
import models.Message;
import models.PublicRoom;
import models.User;
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
import utils.TestUtils;

import javax.inject.Provider;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.libs.Json.fromJson;
import static play.libs.Json.toJson;

/**
 * Created by kdoherty on 8/3/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class RoomActorTest extends WithApplication {

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

    private AnonUser anonUser;

    private static boolean firstTest = true;

    private JsonNode firstEvent;

    private JsonNode secondEvent;

    private UserFactory userFactory;
    private AnonUserFactory anonUserFactory;


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
        ((Map) TestUtils.getHiddenField(RoomActor.class, "rooms")).clear();

        userFactory = new UserFactory();
        user = userFactory.create(PropOverride.of("userId", userId));
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        publicRoom = new PublicRoomFactory().create(PropOverride.of("roomId", roomId));
        when(abstractRoomService.findById(roomId)).thenReturn(Optional.of(publicRoom));
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(publicRoom));

        anonUserFactory = new AnonUserFactory();
        anonUser = anonUserFactory.create();
        when(anonUserService.getOrCreateAnonUser(user, publicRoom)).thenReturn(anonUser);

        jedis = spy(new MockJedis());
        jedisService = new MockJedisService(jedis);

        keepAliveService = spy(Play.current().injector().instanceOf(KeepAliveService.class));
        ActorRef defaultRoomWithMockedServices = Play.current().actorSystem().actorOf(Props.create(RoomActor.class, () -> {
            return spy(new RoomActor(abstractRoomService, userService, anonUserService, messageService, jedisService, keepAliveService));
        }));

        TestUtils.setPrivateStaticFinalField(RoomActor.class, "defaultRoom", defaultRoomWithMockedServices);

        roomSocketService = new RoomSocketServiceImpl(abstractRoomService, publicRoomService, userService, anonUserService, jedisService);
        SecurityService securityService = Play.current().injector().instanceOf(SecurityService.class);
        roomSocketsController = new RoomSocketsController(roomSocketService, privateRoomService, securityService);

        // Hack to do mock static initialization block from RoomActor
        if (((MockJedis) jedis).getSubscribers(RoomActor.CHANNEL).isEmpty() && !firstTest) {
            jedis.subscribe(new RoomActor.MessageListener(), RoomActor.CHANNEL);
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

        boolean event1IsRosterNotification = RoomActor.Join.TYPE.equals(firstEvent.get("event").asText());
        boolean event1IsJoinSuccess = "joinSuccess".equals(firstEvent.get("event").asText());
        boolean event2IsRosterNotification = RoomActor.Join.TYPE.equals(secondEvent.get("event").asText());
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
        assertEquals(user, fromJson(rosterNotification.get("user"), User.class));

        JsonNode message = joinSuccess.get(RoomActor.MESSAGE_KEY);
        User[] roomMembers = fromJson(message.get("roomMembers"), User[].class);
        assertEquals(0, roomMembers.length);
        assertFalse(message.get("isSubscribed").asBoolean());
        AnonUser actualAnonUser = fromJson(message.get("anonUser"), AnonUser.class);
        assertEquals(anonUser, actualAnonUser);
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
        RoomActor.RosterNotification rosterNotify = new RoomActor.RosterNotification(roomId, userId, RoomActor.Join.TYPE);
        verify(jedis).publish(RoomActor.CHANNEL, Json.stringify(toJson(rosterNotify)));
    }

    @Test
    public void joinAddsKeepAlive() throws NoSuchFieldException, IllegalAccessException {
        //assertTrue(keepAliveService.hasKeepAlive(roomId));
        verify(keepAliveService).start(roomId);
    }

    private JsonNode sendMessage(MockWebSocket socket, String message, boolean isAnon, String uuid) throws Throwable {
        JsonNode messageJson = Json.newObject()
                .put("event", RoomActor.Talk.TYPE)
                .put("message", message)
                .put("isAnon", isAnon)
                .put("uuid", uuid);
        socket.write(messageJson);
        return messageJson;
    }

    @Test
    public void talkStoresMessage() throws Throwable {
        String message = "Hello";
        sendMessage(webSocket, message, false, "");

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
        long otherUserId = 6;
        User otherUser = userFactory.create(PropOverride.of("userId", otherUserId));
        when(userService.findById(otherUserId)).thenReturn(Optional.of(otherUser));
        MockWebSocket otherSocket = new MockWebSocket(roomSocketsController.joinPublicRoom(roomId, otherUserId, ""));
        // Join roster notification and joinSuccess events
        otherSocket.read();
        otherSocket.read();


        String message = "Hello";
        sendMessage(webSocket, message, false, "");

        JsonNode talkEvent = otherSocket.read();

        assertEquals(RoomActor.Talk.TYPE, talkEvent.get("event").asText());
        JsonNode messageJson = Json.parse(talkEvent.get("message").asText());
        assertTrue(messageJson.get("messageId").canConvertToLong());
        assertEquals(user, fromJson(messageJson.get("sender"), User.class));
        assertEquals(message, messageJson.get("message").asText());
    }

    @Test
    public void talkConfirmationIsSentOut() throws Throwable {
        String uuid = UUID.randomUUID().toString();
        String message = "Hello";
        sendMessage(webSocket, message, false, uuid);

        JsonNode talkConfirmation = webSocket.read();

        assertEquals("talk-confirmation", talkConfirmation.get("event").asText());
        assertEquals(uuid, talkConfirmation.get("uuid").asText());

        JsonNode messageJson = Json.parse(talkConfirmation.get("message").asText());
        assertTrue(messageJson.get("messageId").canConvertToLong());
        assertEquals(user, fromJson(messageJson.get("sender"), User.class));
        assertEquals(message, messageJson.get("message").asText());
    }

    @Test
    public void anonTalksAreStoredWithAnonSenders() throws Throwable {
        AnonUser anonUser = new AnonUserFactory().create(PropOverride.of("actual", user));
        when(anonUserService.getOrCreateAnonUser(any(), any())).thenReturn(anonUser);

        String message = "Hello";
        sendMessage(webSocket, message, true, "");

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
        RoomActor.remoteMessage(new RoomActor.Talk(roomId, KeepAliveService.ID, KeepAliveService.MSG, ""));
        webSocket.read();
        verify(abstractRoomService, never()).addMessage(any(), any(), any());
    }

    @Test
    public void keepAliveMessagesFormat() throws InterruptedException {
        RoomActor.remoteMessage(new RoomActor.Talk(roomId, KeepAliveService.ID, KeepAliveService.MSG, ""));
        JsonNode keepAliveEvent = webSocket.read();
        assertEquals(RoomActor.Talk.TYPE, keepAliveEvent.get("event").asText());
        assertEquals(KeepAliveService.MSG, keepAliveEvent.get("message").asText());
    }

    private JsonNode sendFavoriteEvent(MockWebSocket ws, long messageId, RoomActor.FavoriteNotification.Action action) throws Throwable {
        JsonNode favoriteEvent = Json.newObject()
                .put("event", RoomActor.FavoriteNotification.TYPE)
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
        RoomActor.FavoriteNotification.Action action = RoomActor.FavoriteNotification.Action.ADD;

        sendFavoriteEvent(webSocket, messageId, action);

        JsonNode favoriteEvent = webSocket.read();
        assertEquals(action.getType(), favoriteEvent.get(RoomActor.EVENT_KEY).asText());
        assertEquals(messageId, favoriteEvent.get(RoomActor.MESSAGE_KEY).asLong());
        assertEquals(user, fromJson(favoriteEvent.get(RoomActor.USER_KEY), User.class));
    }

    @Test
    public void favoriteAMessageThatHasAlreadyBeenFavoritedByThatUser() throws Throwable {
        long messageId = 5;
        Message message = new MessageFactory().create(PropOverride.of("room", publicRoom));
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(messageService.favorite(message, user)).thenReturn(false);
        RoomActor.FavoriteNotification.Action action = RoomActor.FavoriteNotification.Action.ADD;

        sendFavoriteEvent(webSocket, messageId, action);

        JsonNode errorEvent = webSocket.read();
        assertEquals("error", errorEvent.get(RoomActor.EVENT_KEY).asText());
        assertEquals("Problem " + action + "ing a favorite", errorEvent.get(RoomActor.MESSAGE_KEY).asText());
    }


    @Test
    public void removeFavoriteFromAMessage() throws Throwable {
        long messageId = 5;
        Message message = new MessageFactory().create(PropOverride.of("room", publicRoom));
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(messageService.removeFavorite(message, user)).thenReturn(true);
        RoomActor.FavoriteNotification.Action action = RoomActor.FavoriteNotification.Action.REMOVE;

        sendFavoriteEvent(webSocket, messageId, action);

        JsonNode favoriteEvent = webSocket.read();
        assertEquals(action.getType(), favoriteEvent.get(RoomActor.EVENT_KEY).asText());
        assertEquals(messageId, favoriteEvent.get(RoomActor.MESSAGE_KEY).asLong());
        assertEquals(user, fromJson(favoriteEvent.get(RoomActor.USER_KEY), User.class));
    }

    @Test
    public void removeFavoriteFromAMessageHasNotFavorited() throws Throwable {
        long messageId = 5;
        Message message = new MessageFactory().create(PropOverride.of("room", publicRoom));
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(messageService.removeFavorite(message, user)).thenReturn(false);
        RoomActor.FavoriteNotification.Action action = RoomActor.FavoriteNotification.Action.REMOVE;

        sendFavoriteEvent(webSocket, messageId, action);

        JsonNode errorEvent = webSocket.read();
        assertEquals("error", errorEvent.get(RoomActor.EVENT_KEY).asText());
        assertEquals("Problem " + action + "ing a favorite", errorEvent.get(RoomActor.MESSAGE_KEY).asText());
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
        assertEquals(RoomActor.Quit.TYPE, quitEvent.get(RoomActor.EVENT_KEY).asText());
        assertEquals("has left the room", quitEvent.get(RoomActor.MESSAGE_KEY).asText());
        assertEquals(user, fromJson(quitEvent.get(RoomActor.USER_KEY), User.class));
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
                .put("type", RoomActor.Quit.TYPE)
                .put("roomId", roomId)
                .put("userId", userId);
        jedis.publish(RoomActor.CHANNEL, Json.stringify(quit));

        JsonNode quitEvent = otherSocket.read();
        assertEquals(RoomActor.Quit.TYPE, quitEvent.get(RoomActor.EVENT_KEY).asText());
        assertEquals("has left the room", quitEvent.get(RoomActor.MESSAGE_KEY).asText());
        assertEquals(user, fromJson(quitEvent.get(RoomActor.USER_KEY), User.class));
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
