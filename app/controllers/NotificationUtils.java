package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.util.Map;
import java.util.Optional;

import static play.libs.Json.toJson;


public class NotificationUtils {

    public static final String GCM_URL = "https://android.googleapis.com/gcm/send";
    public static final String GCM_API_KEY = "AIzaSyDp2t64B8FsJUAOszaFl14-uiDVoZRu4W4";

    public static final ApnsService service = APNS.newService()
            .withCert("/certificates/dev.cer", "")
            .withSandboxDestination()
            .build();

    private NotificationUtils() { }

    private static class AndroidGcmRequest {

        public String[] registration_ids;
        public ObjectNode data = Json.newObject();

        private AndroidGcmRequest(String[] registration_ids, Map<String, String> dataMap) {
            this.registration_ids = registration_ids;
            setData(dataMap);
        }

        private AndroidGcmRequest(String[] registration_ids) {
            this.registration_ids = registration_ids;
        }

        private void setData(Map<String, String> map) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                data.put(entry.getKey(), entry.getValue());
            }
        }
    }


    public static void sendAppleNotification() {
        String payload = APNS.newPayload().alertBody("message").build();
        String token = "deviceToken";
        service.push(token, payload);
    }


    public static F.Promise<WSResponse> sendAndroidNotification(String[] regIds, Optional<Map<String, String>> dataOptional) {
        AndroidGcmRequest request;
        if (dataOptional.isPresent()) {
            request = new AndroidGcmRequest(regIds, dataOptional.get());
        } else {
            request = new AndroidGcmRequest(regIds);
        }

        Logger.debug("sendAndroidNotification called with json: " + toJson(request));

        return WS.url(GCM_URL)
                .setAuth("key", GCM_API_KEY)
                .setContentType("application/json")
                .setHeader("key", GCM_API_KEY)
                .post(toJson(request));
    }
}
