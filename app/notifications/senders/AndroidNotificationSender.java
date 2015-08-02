package notifications.senders;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.inject.Singleton;
import play.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 6/11/15.
 */
@Singleton
public class AndroidNotificationSender implements NotificationSender {

    private static final String GCM_API_KEY = "AIzaSyC45QxNleXmw_Nf2L2m3bWDoLiTjpTE9wA";
    private static final Sender GCM_SENDER = new Sender(GCM_API_KEY);

    private static final int GCM_RETRIES = 3;

    private static Message buildGcmMessage(Map<String, String> data) {
        Message.Builder builder = new Message.Builder();
        data.entrySet().forEach(entry -> builder.addData(entry.getKey(), entry.getValue()));
        return builder.build();
    }

    @Override
    public void sendNotification(String regId, Map<String, String> data) {
        Message message = buildGcmMessage(data);
        try {
            GCM_SENDER.send(message, regId, GCM_RETRIES);
        } catch (IOException e) {
            Logger.error("Problem sending GCM Message", e);
        }
    }

    @Override
    public void sendBatchNotification(List<String> regIds, Map<String, String> data) {
        Message message = buildGcmMessage(data);
        try {
            GCM_SENDER.send(message, regIds, GCM_RETRIES);
        } catch (IOException e) {
            Logger.error("Problem sending batch GCM message", e);
        }
    }
}
