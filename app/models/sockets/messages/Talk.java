package models.sockets.messages;

public class Talk {

    public static final String TYPE = "talk";

    final long roomId;
    final long userId;
    final String text;

    // For JSON serialization
    final String type = TYPE;

    public Talk(long roomId, long userId, String text) {
        this.roomId = roomId;
        this.userId = userId;
        this.text = text;
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

    @Override
    public String toString() {
        return "Talk (" + roomId + ") " + userId + " - " + text;
    }
}