package notifications;

import com.google.common.collect.ImmutableMap;
import models.entities.Message;
import models.entities.User;

/**
 * Created by kevin on 6/11/15.
 */
public class MessageFavoritedNotification extends AbstractNotification {

    public MessageFavoritedNotification(Message message, User messageFavoritor) {
        super(Event.MESSAGE_FAVORITED, getContent(message, messageFavoritor));
    }

    public static ImmutableMap<String, String> getContent(Message message, User messageFavoritor) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.FACEBOOK_NAME, messageFavoritor.name)
                .put(Key.FACEBOOK_ID, messageFavoritor.facebookId)
                .put(Key.MESSAGE, message.message)
                .putAll(getRoomData(message.room))
                .build();
    }
}
