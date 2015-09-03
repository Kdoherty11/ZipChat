package notifications;

import com.google.common.collect.ImmutableMap;
import models.Request;
import play.libs.Json;

/**
 * Created by kevin on 6/12/15.
 */
public class ChatResponseNotification extends AbstractNotification {

    public ChatResponseNotification(Request request, Request.Status response) {
        super(Event.CHAT_REQUEST_RESPONSE, getContentBuilder(request, response));
    }

    private static ImmutableMap.Builder<String, String> getContentBuilder(Request request, Request.Status response) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.USER, Json.stringify(Json.toJson(request.receiver)))
                .put(Key.CHAT_REQUEST_RESPONSE, response.toString());
    }


}
