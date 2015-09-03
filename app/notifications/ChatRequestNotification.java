package notifications;

import com.google.common.collect.ImmutableMap;
import models.User;
import play.libs.Json;

/**
 * Created by kevin on 6/11/15.
 */
public class ChatRequestNotification extends AbstractNotification {

    public ChatRequestNotification(User sender) {
        super(Event.CHAT_REQUEST, getContentBuilder(sender));
    }

    private static ImmutableMap.Builder<String, String> getContentBuilder(User sender) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.USER, Json.stringify(Json.toJson(sender)));
    }
}
