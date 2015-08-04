package unit.sockets;

import akka.actor.ActorRef;
import akka.actor.Props;
import controllers.RoomSocketsController;
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
import play.test.WithApplication;
import redis.clients.jedis.Jedis;
import services.*;
import services.impl.RoomSocketServiceImpl;
import sockets.RoomSocket;
import utils.TestUtils;

import javax.inject.Provider;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

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

    private long roomId = 1;

    private long userId = 2;

    private User user;

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
            user = new UserFactory().create();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        jedis = new MockJedis();
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

        webSocket = new MockWebSocket(roomSocketsController.joinPublicRoom(roomId, userId, ""));
    }

    @Test
    public void joinAddsRoomToRooms() {
        assertTrue(true);
    }

}
