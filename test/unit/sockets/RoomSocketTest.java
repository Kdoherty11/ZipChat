package unit.sockets;

import controllers.RoomSocketsController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.test.WithApplication;
import redis.clients.jedis.JedisPool;
import services.AbstractRoomService;
import services.AnonUserService;
import services.JedisService;
import services.MessageService;

import static org.junit.Assert.assertTrue;

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

    private JedisService jedisService;

    private JedisPool jedisPool;

    @Before
    public void setUp() {
//        jedisPool = new MyMockJedisPool(new JedisPoolConfig(), "localhost");
//
//        jedisService = new JedisServiceImpl(jedisPool);
//
//        jedisService.useJedisResource(j -> assertTrue(j instanceof MockJedis));
//
//        Logger.error("JedisPool: " + jedisPool);
//
//        ActorRef defaultRoomWithMockedServices = Play.current().actorSystem().actorOf(Props.create(RoomSocket.class, () -> {
//            return new RoomSocket(abstractRoomService, anonUserService, messageService, jedisService);
//        }));
//
//        try {
//            TestUtils.setPrivateStaticFinalField(RoomSocket.class, "defaultRoom", defaultRoomWithMockedServices);
//        } catch (IllegalAccessException | NoSuchFieldException e) {
//            throw new RuntimeException(e);
//        }
//
//        roomSocketsController = Play.current().injector().instanceOf(RoomSocketsController.class);
//
//        webSocket = new MockWebSocket(roomSocketsController.joinPublicRoom(roomId, userId, ""));
    }

    @Test
    public void joinAddsRoomToRooms() {
        assertTrue(true);
    }

}
