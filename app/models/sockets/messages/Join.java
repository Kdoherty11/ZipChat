package models.sockets.messages;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.WebSocket;

public class Join {

    public static final String TYPE = "join";

    private final long roomId;
    private final long username;
    private final WebSocket.Out<JsonNode> channel;

    // For JSON serialization
    final String type = TYPE;

    public Join(long roomId, long username, WebSocket.Out<JsonNode> channel) {
        this.roomId = roomId;
        this.username = username;
        this.channel = channel;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getUsername() {
        return username;
    }

    public String getType() {
        return type;
    }

    public WebSocket.Out<JsonNode> getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "Join (" + roomId + ") from " + username;
    }
}