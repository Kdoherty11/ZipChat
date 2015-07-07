package services;

import com.google.inject.ImplementedBy;
import notifications.AbstractNotification;
import services.impl.NotificationServiceImpl;

import java.util.List;

/**
 * Created by kdoherty on 7/6/15.
 */
@ImplementedBy(NotificationServiceImpl.class)
public interface NotificationService {

    void send(List<String> androidRegIds, List<String> iosRegIds, AbstractNotification notification);

}
