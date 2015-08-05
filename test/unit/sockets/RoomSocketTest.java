package unit.sockets;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.RoomSocketsController;
import factories.PropOverride;
import factories.UserFactory;
import models.PrivateRoom;
import models.PublicRoom;
import models.User;
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
import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
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
    private PublicRoom publicRoom;

    @Mock
    private PrivateRoom privateRoom;

    private MockJedisService jedisService;

    private Jedis jedis;

    private RoomSocketService roomSocketService;

    private final long roomId = 1;

    private final long userId = 2;

    private User user;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static boolean firstTest = true;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(JedisService.class).to(MockJedisService.class))
                .overrides(bind(UserService.class).to((Provider<UserService>) () -> userService))
                .build();
    }

    @Before
    public void setUp() {
        MockJedis.clean();
        try {
            user = new UserFactory().create(PropOverride.of("userId", userId));
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        jedis = spy(new MockJedis());
        jedisService = new MockJedisService(jedis);

        ActorRef defaultRoomWithMockedServices = Play.current().actorSystem().actorOf(Props.create(RoomSocket.class, () -> {
            return new RoomSocket(abstractRoomService, userService, anonUserService, messageService, jedisService);
        }));

        try {
            TestUtils.setPrivateStaticFinalField(RoomSocket.class, "defaultRoom", defaultRoomWithMockedServices);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        roomSocketService = new RoomSocketServiceImpl(abstractRoomService, publicRoomService, jedisService);
        PrivateRoomService privateRoomService = Play.current().injector().instanceOf(PrivateRoomService.class);
        SecurityService securityService = Play.current().injector().instanceOf(SecurityService.class);
        roomSocketsController = new RoomSocketsController(roomSocketService, privateRoomService, securityService);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(abstractRoomService.findById(roomId)).thenReturn(Optional.of(publicRoom));

        // Hack to do mock static initialization block from RoomSocket
        if (((MockJedis)jedis).getSubscribers(RoomSocket.CHANNEL).isEmpty() && !firstTest) {
            jedis.subscribe(new RoomSocket.MessageListener(), RoomSocket.CHANNEL);
        }

        firstTest = false;

        webSocket = new MockWebSocket(roomSocketsController.joinPublicRoom(roomId, userId, ""));
    }

    @Test
    public void joinSendsRosterNotify() throws IOException, InterruptedException {
        JsonNode event1 = webSocket.read();
        JsonNode event2 = webSocket.read();

        JsonNode rosterNotification;
        JsonNode joinSuccess;

        boolean event1IsRosterNotification = RoomSocket.Join.TYPE.equals(event1.get("event").asText());
        boolean event1IsJoinSuccess = "joinSuccess".equals(event1.get("event").asText());
        boolean event2IsRosterNotification = RoomSocket.Join.TYPE.equals(event2.get("event").asText());
        boolean event2IsJoinSuccess = "joinSuccess".equals(event2.get("event").asText());

        if (event1IsRosterNotification && event2IsJoinSuccess) {
            rosterNotification = event1;
            joinSuccess = event2;
        } else if (event1IsJoinSuccess && event2IsRosterNotification) {
            rosterNotification = event2;
            joinSuccess = event1;
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
    public void joinAddsUserToJedis() {
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

}
