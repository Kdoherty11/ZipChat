package models.sockets.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import play.mvc.WebSocket;

public class Join {

    public static final String TYPE = "join";

    private final long roomId;
    private final long userId;
    private final WebSocket.Out<JsonNode> channel;

    // For JSON serialization
    final String type = TYPE;

    public Join(long roomId, long userId, WebSocket.Out<JsonNode> channel) {
        this.roomId = roomId;
        this.userId = userId;
        this.channel = channel;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public WebSocket.Out<JsonNode> getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("roomId", roomId)
                .add("userId", userId)
                .toString();
    }
}