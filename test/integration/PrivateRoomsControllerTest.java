package integration;

import adapters.PrivateRoomsControllerAdapter;
import models.entities.PrivateRoom;
import org.json.JSONException;
import org.junit.Test;

public class PrivateRoomsControllerTest extends AbstractControllerTest {

    private static final PrivateRoomsControllerAdapter adapter = PrivateRoomsControllerAdapter.INSTANCE;

    @Test
    public void testGetPrivateRoomsByUserId() throws JSONException {
        PrivateRoom roomOne = adapter.makePrivateRoom();
        PrivateRoom roomTwo =  adapter.makePrivateRoom();
    }


    @Test
    public void testLeaveRoom() throws JSONException {
        PrivateRoom room = adapter.makePrivateRoom();
    }



}
