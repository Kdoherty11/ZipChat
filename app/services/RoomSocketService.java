package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.ImplementedBy;
import play.mvc.WebSocket;
import services.impl.RoomSocketServiceImpl;

/**
 * Created by kdoherty on 7/13/15.
 */
@ImplementedBy(RoomSocketServiceImpl.class)
public interface RoomSocketService {

    void join(long roomId, final long userId, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception;
}