package notifications.senders;

import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 6/11/15.
 */
public interface NotificationSender {

    void sendNotification(String regId, Map<String, String> data);
    void sendBatchNotification(List<String> regIds, Map<String, String> data);

}
