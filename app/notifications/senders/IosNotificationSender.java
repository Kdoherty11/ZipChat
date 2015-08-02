package notifications.senders;

import com.google.inject.Singleton;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.PayloadBuilder;
import com.notnoop.exceptions.NetworkIOException;
import play.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 6/11/15.
 */

@Singleton
public class IosNotificationSender implements NotificationSender {

    private static final ApnsService APNS_SERVICE = APNS.newService()
            .withCert("certificates/dev.p12", "password")
            .withSandboxDestination()
            .build();

    private String buildAppleMessage(Map<String, String> data) {
        PayloadBuilder builder = PayloadBuilder.newPayload()
                .alertBody("New Message");
        data.entrySet().forEach(entry -> builder.customField(entry.getKey(), entry.getValue()));
        return builder.build();
    }

    @Override
    public void sendNotification(String regId, Map<String, String> data) {
        String payload = buildAppleMessage(data);
        try {
            APNS_SERVICE.push(regId, payload);
        } catch (NetworkIOException e) {
            Logger.error("Problem sending APN", e);
        }
    }

    @Override
    public void sendBatchNotification(List<String> regIds, Map<String, String> data) {
        String payload = buildAppleMessage(data);
        try {
            APNS_SERVICE.push(regIds, payload);
        } catch (NetworkIOException e) {
            Logger.error("Problem sending batch APN", e);
        }
    }
}
