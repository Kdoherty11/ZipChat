package adapters;

import controllers.BaseController;
import controllers.routes;
import models.entities.PrivateRoom;
import models.entities.Request;
import models.entities.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.F;
import play.mvc.Result;
import utils.DbUtils;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

import static play.test.Helpers.callAction;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;

public enum PrivateRoomsControllerAdapter {

    INSTANCE;

    public static final String SENDER_KEY = "sender";
    public static final String RECEIVER_KEY = "receiver";
    public static final String SENDER_IN_ROOM_KEY = "senderInRoom";
    public static final String RECEIVER_IN_ROOM_KEY = "receiverInRoom";
    public static final String REQUEST_KEY = "request";

    private RequestsControllerAdapter requestAdapter = RequestsControllerAdapter.INSTANCE;


    public PrivateRoom makePrivateRoom() throws Throwable {
        long createdResultId = requestAdapter.getCreateRequestId();
        requestAdapter.handleResponse(createdResultId, Request.Status.accepted.toString());
        Optional<Request> requestOptional = DbUtils.findEntityById(Request.class, createdResultId);
        Request request = requestOptional.get();
        final long senderId = request.sender.userId;
        final long receiverId = request.receiver.userId;

        String queryString = "select p from PrivateRoom p where (p.sender.userId = :senderId and p.senderInRoom = true) and (p.receiver.userId = :receiverId and p.receiverInRoom = true)";

        TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
                .setParameter("senderId", senderId)
                .setParameter("receiverId", receiverId);

        List<PrivateRoom> privateRoomList = query.getResultList();
        return privateRoomList.get(0);
    }

    public Result getRoomsByUserId(long userId) {
        return play.test.Helpers.callAction(routes.ref.PrivateRoomsController.getRoomsByUserId(userId), fakeRequest());
    }

    public long getRoomCountByUserId(Result roomsResult) throws JSONException {
        JSONArray json = new JSONArray(contentAsString(roomsResult));
        Logger.debug("JSON: " + json);
        return json.length();
    }

    public long getRoomCountByUserId(long userId) throws JSONException {
        return getRoomCountByUserId(getRoomsByUserId(userId));
    }

    public Result leaveRoom(long roomId, long userId) {
        return play.test.Helpers.callAction(routes.ref.PrivateRoomsController.leaveRoom(roomId, userId), fakeRequest());
    }
}
