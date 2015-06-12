package notifications;

import com.google.common.collect.ImmutableMap;
import models.entities.Message;

/**
 * Created by kevin on 6/11/15.
 */
public class MessageNotification extends AbstractNotification {

    public MessageNotification(Message message) {
        super(Event.CHAT_MESSAGE, getContent(message));
    }

    private static ImmutableMap<String, String> getContent(Message message) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.FACEBOOK_NAME, message.sender.name)
                .put(Key.FACEBOOK_ID, message.sender.facebookId)
                .put(Key.MESSAGE, message.message)
                .putAll(getRoomData(message.room))
                .build();
    }
}
