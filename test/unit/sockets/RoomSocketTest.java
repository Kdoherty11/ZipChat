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
import play.mvc.WebSocket;
import play.test.WithApplication;
import redis.clients.jedis.Jedis;
import services.*;
import services.impl.RoomSocketServiceImpl;
import sockets.KeepAlive;
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

    private PrivateRoom privateRoom;

    private MockJedisService jedisService;

    private Jedis jedis;

    private RoomSocketService roomSocketService;

    private final long roomId = 1;

    private final long userId = 2;

    private User user;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static boolean firstTest = true;

    private JsonNode firstEvent;

    private JsonNode secondEvent;



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

        user = new UserFactory().create(PropOverride.of("userId", userId));
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        publicRoom = new PublicRoomFactory().create(PropOverride.of("roomId", roomId));
        when(abstractRoomService.findById(roomId)).thenReturn(Optional.of(publicRoom));
        when(publicRoomService.findById(roomId)).thenReturn(Optional.of(publicRoom));

        jedis = spy(new MockJedis());
        jedisService = new MockJedisService(jedis);

        ActorRef defaultRoomWithMockedServices = Play.current().actorSystem().actorOf(Props.create(RoomSocket.class, () -> {
            return new RoomSocket(abstractRoomService, userService, anonUserService, messageService, jedisService);
        }));

        TestUtils.setPrivateStaticFinalField(RoomSocket.class, "defaultRoom", defaultRoomWithMockedServices);

        roomSocketService = new RoomSocketServiceImpl(abstractRoomService, publicRoomService, jedisService);
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
        assertEquals(1, roomMembers.length);
        assertEquals(user, roomMembers[0]);
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
    @SuppressWarnings("unchecked")
    public void joinAddsKeepAlive() throws NoSuchFieldException, IllegalAccessException {
        Map<Long, Map<Long, WebSocket.Out<JsonNode>>> rooms = (Map<Long, Map<Long, WebSocket.Out<JsonNode>>>) TestUtils.getHiddenField(RoomSocket.class, "rooms");
        assertTrue(rooms.get(roomId).containsKey(KeepAlive.USER_ID));
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
        RoomSocket.remoteMessage(new RoomSocket.Talk(roomId, KeepAlive.USER_ID, KeepAlive.HEARTBEAT_MESSAGE));
        webSocket.read();
        verify(abstractRoomService, never()).addMessage(any(), any(), any());
    }

    @Test
    public void keepAliveMessagesFormat() throws InterruptedException {
        RoomSocket.remoteMessage(new RoomSocket.Talk(roomId, KeepAlive.USER_ID, KeepAlive.HEARTBEAT_MESSAGE));
        JsonNode keepAliveEvent = webSocket.read();
        assertEquals(RoomSocket.Talk.TYPE, keepAliveEvent.get("event").asText());
        assertEquals(KeepAlive.HEARTBEAT_MESSAGE, keepAliveEvent.get("message").asText());
    }

//    @Test
//    public void anonMessageNotAllowedInPrivateRooms() throws Throwable {
//        long privateRoomId = 3;
//        PrivateRoom privateRoom = new PrivateRoomFactory().create(PropOverride.of("roomId", privateRoomId));
//        when(privateRoomService.findById(privateRoomId)).thenReturn(Optional.of(privateRoom));
//        when(abstractRoomService.findById(privateRoomId)).thenReturn(Optional.of(privateRoom));
//
//        MockWebSocket privateRoomWs = new MockWebSocket(roomSocketsController.joinPrivateRoom(privateRoomId, userId, ""));
//        sendMessage(privateRoomWs, "msg", true);
//    }
}
