package notifications;

import com.google.common.collect.ImmutableMap;
import models.Message;
import play.libs.Json;

/**
 * Created by kevin on 6/11/15.
 */
public class MessageNotification extends AbstractNotification {

    public MessageNotification(Message message) {
        super(Event.CHAT_MESSAGE, getContentBuilder(message));
    }

    private static ImmutableMap.Builder<String, String> getContentBuilder(Message message) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.MESSAGE, Json.stringify(Json.toJson(message)))
                .put(Key.ROOM, Json.stringify(Json.toJson(message.room)));
    }
}
