package notifications;

import com.google.common.collect.ImmutableMap;
import models.entities.User;

import java.util.Map;

/**
 * Created by kevin on 6/11/15.
 */
public class ChatRequestNotification extends AbstractNotification {

    public ChatRequestNotification(User sender) {
        super(Event.CHAT_REQUEST, getContent(sender));
    }

    private static Map<String, String> getContent(User sender) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.FACEBOOK_NAME, sender.name)
                .put(Key.FACEBOOK_ID, sender.facebookId)
                .build();
    }
}
