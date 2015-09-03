package notifications;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by kevin on 6/11/15.
 */
public abstract class AbstractNotification {

    protected static class Key {
        private static final String EVENT = "event";
        protected static final String USER = "user";
        protected static final String ROOM = "room";
        protected static final String MESSAGE = "message";
        protected static final String CHAT_REQUEST_RESPONSE = "response";
    }

    protected static class Event {
        protected static final String CHAT_REQUEST = "Chat Request";
        protected static final String CHAT_REQUEST_RESPONSE = "Chat Request Response";
        protected static final String CHAT_MESSAGE = "Chat Message";
        protected static final String MESSAGE_FAVORITED = "Message Favorited";
    }

    private Map<String, String> content;

    public AbstractNotification(String event, ImmutableMap.Builder<String, String> contentBuilder) {
        this.content = contentBuilder.put(Key.EVENT, event).build();
    }

    public Map<String, String> getContent() {
        return content;
    }
}
