package unit.services;

import daos.MessageDao;
import factories.MessageFactory;
import factories.UserFactory;
import models.entities.Message;
import models.entities.User;
import notifications.MessageFavoritedNotification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.MessageService;
import services.UserService;
import services.impl.MessageServiceImpl;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;

/**
 * Created by kdoherty on 7/6/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    private MessageService messageService;
    private MessageFactory messageFactory;
    private UserFactory userFactory;

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserService userService;

    @Before
    public void setUp() {
        messageService = new MessageServiceImpl(messageDao, userService);
        messageFactory = new MessageFactory();
        userFactory = new UserFactory();
    }

    @Test
    public void favoriteIncrementsScore() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User favoritor = userFactory.create();
        messageService.favorite(message, favoritor);

        assertThat(message.score).isEqualTo(1);
    }

    @Test
    public void favoriteSendsNotificationToMessageSender() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);

        User favoritor = userFactory.create();

        messageService.favorite(message, favoritor);

        verify(userService).sendNotification(refEq(message.sender.getActual()), any(MessageFavoritedNotification.class));
    }


    @Test
    public void favoriteFalse() {

    }

    @Test
    public void removeFavoriteTrue() {

    }

    @Test
    public void removeFavoriteFalse() {

    }

    @Test
    public void flagTrue() {

    }

    @Test
    public void flagFalse() {

    }


}
