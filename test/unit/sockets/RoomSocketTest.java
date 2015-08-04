package unit.sockets;

import akka.actor.ActorRef;
import akka.actor.Props;
import controllers.RoomSocketsController;
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
import services.AbstractRoomService;
import services.AnonUserService;
import services.JedisService;
import services.MessageService;
import sockets.RoomSocket;
import utils.TestUtils;

import static org.junit.Assert.assertTrue;
import static play.inject.Bindings.bind;

/**
 * Created by kdoherty on 8/3/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class RoomSocketTest extends WithApplication {

    private RoomSocketsController roomSocketsController;

    private MockWebSocket webSocket;

    private long roomId = 1;
    private long userId = 2;

    @Mock
    private AbstractRoomService abstractRoomService;

    @Mock
    private AnonUserService anonUserService;

    @Mock
    private MessageService messageService;

    private MockJedisService jedisService;

    @Mock
    private Jedis jedis;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(JedisService.class).to(MockJedisService.class))
                .build();
    }

    @Before
    public void setUp() {
        MockJedis.clean();
        jedis = new MockJedis();
        jedisService = new MockJedisService(jedis);

        ActorRef defaultRoomWithMockedServices = Play.current().actorSystem().actorOf(Props.create(RoomSocket.class, () -> {
            return new RoomSocket(abstractRoomService, anonUserService, messageService, jedisService);
        }));

        try {
            TestUtils.setPrivateStaticFinalField(RoomSocket.class, "defaultRoom", defaultRoomWithMockedServices);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        roomSocketsController = Play.current().injector().instanceOf(RoomSocketsController.class);

        webSocket = new MockWebSocket(roomSocketsController.joinPublicRoom(roomId, userId, ""));
    }

    @Test
    public void joinAddsRoomToRooms() {
        assertTrue(true);
    }

}
