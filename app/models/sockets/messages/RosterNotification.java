package models.sockets.messages;

import com.google.common.base.Objects;

public class RosterNotification {

    public static final String TYPE = "rosterNotify";

    final long roomId;
    final long userId;
    final String direction;

    // For JSON serialization
    final String type = TYPE;

    public RosterNotification(long roomId, long userId, String direction) {
        this.roomId = roomId;
        this.userId = userId;
        this.direction = direction;
    }

    public long getUserId() {
        return userId;
    }

    public String getDirection() {
        return direction;
    }

    public long getRoomId() {
        return roomId;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("roomId", roomId)
                .add("userId", userId)
                .add("direction", direction)
                .toString();
    }
}