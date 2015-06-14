package models.sockets.messages;

import com.google.common.base.Objects;


public class Quit {

    public static final String TYPE = "quit";

    private final long roomId;
    private final long userId;

    // For JSON serialization
    final String type = TYPE;

    public Quit(long roomId, long userId) {
        this.roomId = roomId;
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
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
                .toString();
    }

}