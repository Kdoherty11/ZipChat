package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.PayloadBuilder;
import com.notnoop.exceptions.NetworkIOException;
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

    public static final ApnsService SERVICE = APNS.newService()
            .withCert("certificates/dev.p12", "password")
            .withSandboxDestination()
            .build();

    private NotificationUtils() {
    }

    private static Message buildGcmMessage(Map<String, String> data) {
        Message.Builder builder = new Message.Builder();

        data.entrySet().forEach(entry -> builder.addData(entry.getKey(), entry.getValue()));

        return builder.build();
    }

    private static String buildAppleMessage(Map<String, String> data) {
        PayloadBuilder builder = PayloadBuilder.newPayload();
        builder.alertBody("New message");

        data.entrySet().forEach(entry -> builder.customField(entry.getKey(), entry.getValue()));
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

    public static F.Promise<JsonNode> sendAppleNotification(String token, Map<String, String> data) {
        String payload = buildAppleMessage(data);

        //"a1559c63af6a6da908667946561be8795fae109e49ac7ec2e8b27e629b004aa4";
        try {
            SERVICE.push(token, payload);
            return F.Promise.promise(() -> toJson("OK"));
        } catch (NetworkIOException e) {
            Logger.error("Problem sending APN " + e.getMessage());
            return F.Promise.promise(() -> toJson("Failed"));
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

    public static F.Promise<JsonNode> sendBatchAppleNotifications(List<String> tokens, Map<String, String> data) {
        String payload = buildAppleMessage(data);
        try {
            SERVICE.push(tokens, payload);
            return F.Promise.promise(() -> toJson("OK"));
        } catch (NetworkIOException e) {
            return F.Promise.promise(() -> toJson("Failed"));
        }
    }
}
