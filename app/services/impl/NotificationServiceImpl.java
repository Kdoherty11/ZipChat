package services.impl;

import com.google.inject.Inject;
import notifications.AbstractNotification;
import notifications.senders.AndroidNotificationSender;
import notifications.senders.IosNotificationSender;
import services.NotificationService;

import java.util.List;
import java.util.Map;

/**
 * Created by kdoherty on 7/6/15.
 */
public class NotificationServiceImpl implements NotificationService {

    private final AndroidNotificationSender androidNotificationSender;
    private final IosNotificationSender iosNotificationSender;

    @Inject
    public NotificationServiceImpl(AndroidNotificationSender androidNotificationSender,
                                   IosNotificationSender iosNotificationSender) {
        this.androidNotificationSender = androidNotificationSender;
        this.iosNotificationSender = iosNotificationSender;
    }

    @Override
    public void send(List<String> androidRegIds, List<String> iosRegIds, AbstractNotification notification) {
        Map<String, String> content = notification.getContent();

        int numAndroidRegIds = androidRegIds.size();
        if (numAndroidRegIds == 1) {
            androidNotificationSender.sendNotification(androidRegIds.get(0), content);
        } else if (numAndroidRegIds > 1) {
            androidNotificationSender.sendBatchNotification(androidRegIds, content);
        }

        int numIosRegIds = iosRegIds.size();
        if (numIosRegIds == 1) {
            iosNotificationSender.sendNotification(iosRegIds.get(0), content);
        } else if (numIosRegIds > 1) {
            iosNotificationSender.sendBatchNotification(iosRegIds, content);
        }
    }
}
