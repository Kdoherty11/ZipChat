package notifications;

import com.google.common.collect.ImmutableMap;
import models.entities.Request;

import java.util.Map;

/**
 * Created by kevin on 6/12/15.
 */
public class ChatResponseNotification extends AbstractNotification {

    public ChatResponseNotification(Request request, Request.Status response) {
        super(Event.CHAT_REQUEST_RESPONSE, getContent(request, response));
    }

    private static Map<String, String> getContent(Request request, Request.Status response) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.FACEBOOK_NAME, request.receiver.name)
                .put(Key.CHAT_REQUEST_RESPONSE, response.toString())
                .build();
    }


}
