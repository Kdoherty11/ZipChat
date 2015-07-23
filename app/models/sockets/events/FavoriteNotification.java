package models.sockets.events;

import com.google.common.base.Objects;

/**
 * Created by kevin on 5/10/15.
 */
public class FavoriteNotification {

    public static final String TYPE = "FavoriteNotification";

    public enum Action {

        ADD("favorite"),
        REMOVE("removeFavorite");

        private String type;

        Action(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private long messageId;
    private long userId;
    private Action action;

    // For JSON serialization
    final String type = TYPE;

    public FavoriteNotification(long userId, long messageId, Action action) {
        this.userId = userId;
        this.messageId = messageId;
        this.action = action;
    }

    public FavoriteNotification(long userId, long messageId, String actionString) {
        this(userId, messageId, Action.valueOf(actionString.toUpperCase()));
    }

    public long getMessageId() {
        return messageId;
    }

    public long getUserId() {
        return userId;
    }

    public Action getAction() {
        return action;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("messageId", messageId)
                .add("userId", userId)
                .add("action", action)
                .add("type", type)
                .toString();
    }
}
