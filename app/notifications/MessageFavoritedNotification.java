package notifications;

import com.google.common.collect.ImmutableMap;
import models.Message;
import models.User;
import play.libs.Json;

/**
 * Created by kevin on 6/11/15.
 */
public class MessageFavoritedNotification extends AbstractNotification {

    public MessageFavoritedNotification(Message message, User messageFavoritor) {
        super(Event.MESSAGE_FAVORITED, getContentBuilder(message, messageFavoritor));
    }

    public static ImmutableMap.Builder<String, String> getContentBuilder(Message message, User messageFavoritor) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.USER, Json.stringify(Json.toJson(messageFavoritor)))
                .put(Key.MESSAGE, Json.stringify(Json.toJson(message)))
                .put(Key.ROOM, Json.stringify(Json.toJson(message.room)));
    }
}
