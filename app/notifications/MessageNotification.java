package notifications;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import models.entities.Message;

/**
 * Created by kevin on 6/11/15.
 */
public class MessageNotification extends AbstractNotification {

    public MessageNotification(Message message) {
        super(Event.CHAT_MESSAGE, getContentBuilder(message));
    }

    private static ImmutableMap.Builder<String, String> getContentBuilder(Message message) {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>()
                .put(Key.FACEBOOK_NAME, message.sender.name)
                .put(Key.MESSAGE, message.message)
                .putAll(getRoomData(message.room));

        if (!Strings.isNullOrEmpty(message.sender.facebookId)) {
            builder.put(Key.FACEBOOK_ID, message.sender.facebookId);
        }

        return builder;
    }
}
