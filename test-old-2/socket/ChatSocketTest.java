package socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import factories.ObjectFactory;
import integration.AbstractTest;
import models.entities.PrivateRoom;
import models.entities.PublicRoom;
import models.entities.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertEquals;

/**
 * Created by kdoherty on 6/27/15.
 */
@Ignore
public class ChatSocketTest extends AbstractTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ObjectFactory<User> userFactory;
    private ObjectFactory<PublicRoom> publicRoomFactory;
    private ObjectFactory<PrivateRoom> privateRoomFactory;


    private static <T> T readPojo(MockWebSocket socket, Class<T> clazz) throws JsonProcessingException,
            InterruptedException {
        JsonNode data = socket.read();
        assertEquals(data).isNotNull();

        try {
            return objectMapper.convertValue(data, clazz);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JSON: " + objectMapper.writeValueAsString(data), e);
        }
    }

    @Before
    public void initUserFactory() {
        userFactory = new ObjectFactory<>(User.class);
        publicRoomFactory = new ObjectFactory<>(PublicRoom.class);
        privateRoomFactory = new ObjectFactory<>(PrivateRoom.class);
    }

    @After
    public void cleanUp() {
        userFactory.cleanUp();
        userFactory = null;

        publicRoomFactory.cleanUp();
        publicRoomFactory = null;

        privateRoomFactory.cleanUp();
        privateRoomFactory = null;
    }

    @Test
    public void testJoinRoom() throws Throwable {
        User user = userFactory.create();

        publicRoomFactory = new ObjectFactory<>(PublicRoom.class);
        PublicRoom room = publicRoomFactory.create();

        //MockWebSocket firstSocket = new MockWebSocket(PublicRoomsController.joinRoom(user.userId, room.roomId, ""));

        //JsonNode data = firstSocket.read();

        //Logger.debug("!!! GOT RESPONSE: " + data.asText());
    }
}
