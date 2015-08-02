package notifications;

import com.google.common.collect.ImmutableMap;
import models.User;

/**
 * Created by kevin on 6/11/15.
 */
public class ChatRequestNotification extends AbstractNotification {

    public ChatRequestNotification(User sender) {
        super(Event.CHAT_REQUEST, getContentBuilder(sender));
    }

    private static ImmutableMap.Builder<String, String> getContentBuilder(User sender) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.FACEBOOK_NAME, sender.name)
                .put(Key.FACEBOOK_ID, sender.facebookId);
    }
}
