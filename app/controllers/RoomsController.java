package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.sockets.RoomSocket;
import models.entities.AbstractRoom;
import models.entities.Message;
import models.entities.PublicRoom;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.DbUtils;

import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;

public class RoomsController extends BaseController {

    @Transactional
    public static Result createMessage(long roomId) {
        Map<String, String> data = form().bindFromRequest().data();

        String senderIdKey = "sender";
        String messageKey = "message";

        if (!data.containsKey(senderIdKey)) {
            return badRequestJson(senderIdKey + " is required");
        }

        if (!data.containsKey(messageKey)) {
            return badRequestJson(messageKey + " is required");
        }

        long userId = checkId(data.get(senderIdKey));
        if (userId == INVALID_ID) {
            return badRequestJson(senderIdKey + " must be a positive long");
        }

        Optional<AbstractRoom> roomOptional = DbUtils.findEntityById(AbstractRoom.class, roomId);
        if (roomOptional.isPresent()) {

            Message message = new Message(roomOptional.get(), userId, data.get(messageKey));
            message.addToRoom();

            return okJson(message);
        } else {
            return DbUtils.getNotFoundResult(PublicRoom.ENTITY_NAME, roomId);
        }
    }

    @Transactional
    public static WebSocket<JsonNode> joinRoom(final long roomId, final long userId) {

        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                Logger.debug("joining " + roomId + " " + userId);
                try {
                    RoomSocket.join(roomId, userId, in, out);
                } catch (Exception ex) {
                    Logger.error("Problem joining the RoomSocket: " + ex.getMessage());
                }
            }
        };
    }
}
