package utils;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.PayloadBuilder;
import com.notnoop.exceptions.NetworkIOException;
import controllers.BaseController;
import play.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;


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

    private static String buildAppleMessage(Map<String, String> data) {
        PayloadBuilder builder = PayloadBuilder.newPayload();

        builder.alertBody("New message");
        data.entrySet().forEach(entry -> builder.customField(entry.getKey(), entry.getValue()));

        return builder.build();
    }

    public static String sendAppleNotification(String token, Map<String, String> data) {
        String payload = buildAppleMessage(data);
        try {
            SERVICE.push(token, payload);
            return BaseController.OK;
        } catch (NetworkIOException e) {
            String error = "Problem sending APN " + e.getMessage();
            Logger.error(error);
            return error;
        }
    }

    public static String sendBatchAppleNotifications(List<String> tokens, Map<String, String> data) {
        String payload = buildAppleMessage(data);
        try {
            SERVICE.push(tokens, payload);
            return BaseController.OK;
        } catch (NetworkIOException e) {
            String error = "Problem sending batch APN " + e.getMessage();
            Logger.error(error);
            return error;
        }
    }

    private static Message buildGcmMessage(Map<String, String> data) {
        Message.Builder builder = new Message.Builder();

        data.entrySet().forEach(entry -> builder.addData(entry.getKey(), entry.getValue()));

        return builder.build();
    }

    public static String sendAndroidNotification(String regId, Map<String, String> data) {
        Message message = buildGcmMessage(data);

        try {
            GCM_SENDER.send(message, regId, GCM_RETRIES);
            return BaseController.OK;
        } catch (IOException e) {
            String error = "Problem sending GCM Message " + e.getMessage();
            Logger.error(error);
            return error;
        }
    }

    public static String sendBatchAndroidNotifications(List<String> regIds, Map<String, String> data) {
        Message message = buildGcmMessage(data);

        try {
            GCM_SENDER.send(message, regIds, GCM_RETRIES);
            return BaseController.OK;
        } catch (IOException e) {
            String error = "Problem sending batch GCM message " + e.getMessage();
            Logger.error(error);
            return error;
        }
    }
}
