package unit.services;

import notifications.AbstractNotification;
import notifications.senders.AndroidNotificationSender;
import notifications.senders.IosNotificationSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.NotificationService;
import services.impl.NotificationServiceImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 7/7/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {

    private NotificationService notificationService;

    @Mock
    private AndroidNotificationSender androidNotificationSender;

    @Mock
    private IosNotificationSender iosNotificationSender;

    @Before
    public void setUp() {
        notificationService = new NotificationServiceImpl(androidNotificationSender, iosNotificationSender);
    }

    @Test
    public void sendWithSingleAndroidRegIdSendsNonBatchNotification() {
        String androidRegId = "androidRegId";
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        Map<String, String> content = Collections.emptyMap();
        when(mockNotification.getContent()).thenReturn(content);

        notificationService.send(Collections.singletonList(androidRegId), Collections.emptyList(), mockNotification);

        verify(androidNotificationSender).sendNotification(androidRegId, content);
        verifyZeroInteractions(iosNotificationSender);
    }

    @Test
    public void sendWithMultipleAndroidRegIdsSendsBatchNotification() {
        List<String> androidRegIds = Arrays.asList("reg1", "reg2", "reg3");
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        Map<String, String> content = Collections.emptyMap();
        when(mockNotification.getContent()).thenReturn(content);

        notificationService.send(androidRegIds, Collections.emptyList(), mockNotification);

        verify(androidNotificationSender).sendBatchNotification(androidRegIds, content);
        verifyZeroInteractions(iosNotificationSender);
    }

    @Test
    public void sendWithSingleIosRegIdSendsNonBatchNotification() {
        String iosRegId = "iosRegId";
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        Map<String, String> content = Collections.emptyMap();
        when(mockNotification.getContent()).thenReturn(content);

        notificationService.send(Collections.emptyList(), Collections.singletonList(iosRegId), mockNotification);

        verify(iosNotificationSender).sendNotification(iosRegId, content);
        verifyZeroInteractions(androidNotificationSender);
    }

    @Test
    public void sendWithMultipleIosRegIdsSendsBatchNotification() {
        List<String> iosRegIds = Arrays.asList("reg1", "reg2", "reg3");
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        Map<String, String> content = Collections.emptyMap();
        when(mockNotification.getContent()).thenReturn(content);

        notificationService.send(Collections.emptyList(), iosRegIds, mockNotification);

        verify(iosNotificationSender).sendBatchNotification(iosRegIds, content);
        verifyZeroInteractions(androidNotificationSender);
    }

    @Test
    public void sendWithIosAndAndroidRegIdsSendsToBothTypesOfDevices() {
        String androidRegId = "androidRegId";
        String iosRegId = "iosRegId";
        AbstractNotification mockNotification = mock(AbstractNotification.class);
        Map<String, String> content = Collections.emptyMap();
        when(mockNotification.getContent()).thenReturn(content);

        notificationService.send(Collections.singletonList(androidRegId), Collections.singletonList(iosRegId), mockNotification);

        verify(androidNotificationSender).sendNotification(androidRegId, content);
        verify(iosNotificationSender).sendNotification(iosRegId, content);
    }


}
