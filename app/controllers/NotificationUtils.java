package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import play.Logger;
import play.libs.F;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static play.libs.Json.toJson;


public class NotificationUtils {

    public static final String GCM_URL = "https://android.googleapis.com/gcm/send";
    public static final String GCM_API_KEY = "AIzaSyDp2t64B8FsJUAOszaFl14-uiDVoZRu4W4";
    private static final int GCM_RETRIES = 3;

    private static final Sender GCM_SENDER = new Sender(GCM_API_KEY);

    public static final ApnsService service = APNS.newService()
            .withCert("/certificates/dev.cer", "")
            .withSandboxDestination()
            .build();

    private NotificationUtils() { }

    public static void sendAppleNotification() {
        String payload = APNS.newPayload().alertBody("message").build();
        String token = "deviceToken";
        service.push(token, payload);
    }

    private static Message buildGcmMessage(Optional<Map<String, String>> dataOptional) {
        Message.Builder builder = new Message.Builder();

        if (dataOptional.isPresent()) {
            Map<String, String> data = dataOptional.get();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                builder.addData(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    public static F.Promise<JsonNode> sendAndroidNotification(String regId, Optional<Map<String, String>> dataOptional) {
        Message message = buildGcmMessage(dataOptional);

        try {
            Result result = GCM_SENDER.send(message, regId, GCM_RETRIES);
            return F.Promise.promise(() -> toJson(result));
        } catch (IOException e) {
            Logger.error("Problem sending GCM Message " + e.getMessage());
            return F.Promise.promise(() -> toJson("GCM Error: " + e.getMessage()));
        }
    }

    public static F.Promise<JsonNode> sendAndroidMulticastNotification(List<String> regIds, Optional<Map<String, String>> dataOptional) {
        Message message = buildGcmMessage(dataOptional);

        try {
            MulticastResult result = GCM_SENDER.send(message, regIds, GCM_RETRIES);
            return F.Promise.promise(() -> toJson(result.getResults()));
        } catch (IOException e) {
            Logger.error("Problem sending GCM multicast message " + e.getMessage());
            return F.Promise.promise(() -> toJson("GCM Multicast Error: " + e.getMessage()));
        }
    }
}
