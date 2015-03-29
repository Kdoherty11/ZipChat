package adapters;

import models.entities.PrivateRoom;
import org.json.JSONException;

public enum PrivateRoomsControllerAdapter {

    INSTANCE;

    public static final String SENDER_KEY = "sender";
    public static final String RECEIVER_KEY = "receiver";
    public static final String SENDER_IN_ROOM_KEY = "senderInRoom";
    public static final String RECEIVER_IN_ROOM_KEY = "receiverInRoom";
    public static final String REQUEST_KEY = "request";


    public PrivateRoom makePrivateRoom() throws JSONException {
        return new PrivateRoom(RequestsControllerAdapter.INSTANCE.makeRequest());
    }




}
