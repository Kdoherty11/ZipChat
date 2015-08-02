package unit.notifications.senders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.notnoop.apns.ApnsService;
import com.notnoop.exceptions.NetworkIOException;
import notifications.senders.IosNotificationSender;
import notifications.senders.NotificationSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import utils.TestUtils;

import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 8/2/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class IosNotificationSenderTest {

    private static final String regId = "myRegId";
    private static final List<String> regIds = ImmutableList.of("regId1", "regId2");
    private static final Map<String, String> notificationData = ImmutableMap.of("a", "b", "c", "d");
    private static final String expectedPayload = "{\"a\":\"b\",\"c\":\"d\",\"aps\":{\"alert\":\"New Message\"}}";

    private NotificationSender notificationSender;

    @Mock
    private ApnsService apnsService;

    @Before
    public void setUp() {
        notificationSender = new IosNotificationSender();

        try {
            TestUtils.setPrivateStaticFinalField(IosNotificationSender.class, "APNS_SERVICE", apnsService);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void sendNotification() {
        doNothing().when(apnsService).push(anyString(), anyString());
        notificationSender.sendNotification(regId, notificationData);
        verify(apnsService).push(eq(regId), eq(expectedPayload));
    }

    @Test
    public void sendNotificationCatchesException() {
        doThrow(NetworkIOException.class).when(apnsService).push(anyString(), anyString());
        notificationSender.sendNotification(regId, notificationData);
    }

    @Test
    public void sendBatchNotification() {
        doNothing().when(apnsService).push(anyListOf(String.class), anyString());
        notificationSender.sendBatchNotification(regIds, notificationData);
        verify(apnsService).push(eq(regIds), eq(expectedPayload));
    }

    @Test
    public void sendBatchNotificationCatchesException() {
        doThrow(NetworkIOException.class).when(apnsService).push(anyListOf(String.class), anyString());
        notificationSender.sendBatchNotification(regIds, notificationData);
    }
}
