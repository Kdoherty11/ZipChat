package unit.notifications.senders;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import notifications.senders.AndroidNotificationSender;
import notifications.senders.NotificationSender;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import utils.TestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 8/1/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class AndroidNotificationSenderTest {

    private static final int GCM_RETRIES;

    static {
        try {
            GCM_RETRIES = (int) TestUtils.getHiddenField(AndroidNotificationSender.class, "GCM_RETRIES");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private NotificationSender notificationSender;

    @Mock
    private Sender sender;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        notificationSender = new AndroidNotificationSender();

        TestUtils.setPrivateStaticFinalField(AndroidNotificationSender.class, "GCM_SENDER", sender);
    }

    private class NotificationDataMatcher extends TypeSafeMatcher<Message> {

        private final Map<String, String> notificationData;

        private NotificationDataMatcher(Map<String, String> notificationData) {
            this.notificationData = notificationData;
        }

        @Override
        protected boolean matchesSafely(Message item) {
            Map<String, String> data = item.getData();
            return notificationData.size() == data.size() &&
                    notificationData.entrySet().containsAll(data.entrySet());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("notification data does not match");
        }
    }

    @Test
    public void sendNotification() throws IOException, NoSuchFieldException, IllegalAccessException {
        String regId = "myRegId";
        Map<String, String> notificationData = ImmutableMap.of("a", "b", "c", "d");
        when(sender.send(any(), eq(regId), eq(GCM_RETRIES))).thenReturn(null);

        notificationSender.sendNotification(regId, notificationData);

        verify(sender).send(argThat(new NotificationDataMatcher(notificationData)), eq(regId), eq(GCM_RETRIES));
    }

    @Test
    public void sendNotificationCatchesException() throws IOException, NoSuchFieldException, IllegalAccessException {
        String regId = "myRegId";
        Map<String, String> notificationData = ImmutableMap.of("a", "b", "c", "d");
        doThrow(IOException.class).when(sender).send(any(), anyString(), anyInt());
        notificationSender.sendNotification(regId, notificationData);

        verify(sender).send(any(), anyString(), anyInt());
        // If we got here an error was not thrown
    }

    @Test
    public void sendBatchNotification() throws NoSuchFieldException, IllegalAccessException, IOException {
        List<String> regIds = ImmutableList.of("myRegId", "yourRegId");
        Map<String, String> notificationData = ImmutableMap.of("a", "b", "c", "d");
        when(sender.send(any(), eq(regIds), eq(GCM_RETRIES))).thenReturn(null);

        notificationSender.sendBatchNotification(regIds, notificationData);

        verify(sender).send(argThat(new NotificationDataMatcher(notificationData)), eq(regIds), eq(GCM_RETRIES));
    }

    @Test
    public void sendBatchNotificationCatchesException() throws IOException, NoSuchFieldException, IllegalAccessException {
        List<String> regIds = ImmutableList.of("myRegId", "yourRegId");
        Map<String, String> notificationData = ImmutableMap.of("a", "b", "c", "d");
        doThrow(IOException.class).when(sender).send(any(), anyListOf(String.class), anyInt());
        notificationSender.sendBatchNotification(regIds, notificationData);

        verify(sender).send(any(), anyListOf(String.class), anyInt());
        // If we got here an error was not thrown
    }
}
