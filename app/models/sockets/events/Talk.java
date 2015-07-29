package models.sockets.events;

import com.google.common.base.MoreObjects;

public class Talk {

    public static final String TYPE = "talk";

    final long roomId;
    final long userId;
    final String text;
    final boolean isAnon;

    // For JSON serialization
    final String type = TYPE;

    public Talk(long roomId, long userId, String text, boolean isAnon) {
        this.roomId = roomId;
        this.userId = userId;
        this.text = text;
        this.isAnon = isAnon;
    }

    public Talk(long roomId, long userId, String text) {
        this(roomId, userId, text, false);
    }

    public long getUserId() {
        return userId;
    }

    public String getText() {
        return text;
    }

    public long getRoomId() {
        return roomId;
    }

    public String getType() {
        return type;
    }

    public boolean isAnon() {
        return isAnon;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("roomId", roomId)
                .add("userId", userId)
                .add("text", text)
                .add("isAnon", isAnon)
                .toString();
    }
}