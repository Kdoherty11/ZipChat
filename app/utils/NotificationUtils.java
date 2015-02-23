package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import play.Logger;
import play.libs.F;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static play.libs.Json.toJson;


public class NotificationUtils {

    public static final String GCM_API_KEY = "AIzaSyDp2t64B8FsJUAOszaFl14-uiDVoZRu4W4";
    private static final int GCM_RETRIES = 3;

    private static final Sender GCM_SENDER = new Sender(GCM_API_KEY);

//    public static final ApnsService service = APNS.newService()
//            .withCert("/certificates/dev.cer", "")
//            .withSandboxDestination()
//            .build();

    private NotificationUtils() {
    }

    public static void sendAppleNotification() {
        String payload = APNS.newPayload().alertBody("message").build();
        String token = "deviceToken";
        //service.push(token, payload);
    }

    private static Message buildGcmMessage(Map<String, String> data) {
        Message.Builder builder = new Message.Builder();

        data.entrySet().forEach(entry -> builder.addData(entry.getKey(), entry.getValue()) );

        return builder.build();
    }

    public static F.Promise<JsonNode> sendAndroidNotification(String regId, Map<String, String> data) {
        Message message = buildGcmMessage(data);

        try {
            Result result = GCM_SENDER.send(message, regId, GCM_RETRIES);
            return F.Promise.promise(() -> toJson(result));
        } catch (IOException e) {
            Logger.error("Problem sending GCM Message " + e.getMessage());
            return F.Promise.promise(() -> toJson("GCM Error: " + e.getMessage()));
        }
    }

    public static F.Promise<JsonNode> sendBatchAndroidNotifications(List<String> regIds, Map<String, String> data) {
        Message message = buildGcmMessage(data);

        try {
            MulticastResult result = GCM_SENDER.send(message, regIds, GCM_RETRIES);
            return F.Promise.promise(() -> toJson(result.getResults()));
        } catch (IOException e) {
            Logger.error("Problem sending GCM multicast message " + e.getMessage());
            return F.Promise.promise(() -> toJson("GCM Multicast Error: " + e.getMessage()));
        }
    }
}
