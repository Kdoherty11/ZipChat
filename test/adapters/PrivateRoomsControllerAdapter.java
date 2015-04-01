package adapters;

import controllers.routes;
import models.entities.PrivateRoom;
import org.json.JSONException;
import play.mvc.Result;

import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeRequest;

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

    public Result getRoomsByUserId(long userId) {
        return callAction(routes.ref.PrivateRoomsController.getRoomsByUserId(userId), fakeRequest());
    }

    public Result leaveRoom(long roomId, long userId) {
        return callAction(routes.ref.PrivateRoomsController.leaveRoom(roomId, userId), fakeRequest());
    }
}
