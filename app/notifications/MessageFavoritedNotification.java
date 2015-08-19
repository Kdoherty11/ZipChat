package notifications;

import com.google.common.collect.ImmutableMap;
import models.Message;
import models.User;

/**
 * Created by kevin on 6/11/15.
 */
public class MessageFavoritedNotification extends AbstractNotification {

    public MessageFavoritedNotification(Message message, User messageFavoritor) {
        super(Event.MESSAGE_FAVORITED, getContentBuilder(message, messageFavoritor));
    }

    public static ImmutableMap.Builder<String, String> getContentBuilder(Message message, User messageFavoritor) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.FACEBOOK_NAME, messageFavoritor.name)
                .put(Key.FACEBOOK_ID, messageFavoritor.facebookId)
                .put(Key.USER_ID, Long.toString(messageFavoritor.userId))
                .put(Key.MESSAGE, message.message)
                .putAll(getRoomData(message.room));
    }
}
