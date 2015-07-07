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
import static org.mockito.Mockito.never;
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

        verify(userService).sendNotification(refEq((User) message.sender), any(MessageFavoritedNotification.class));
    }

    @Test
    public void favoriteSendsNotificationToAnonSender() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_ANON_SENDER);
        User favoritor = userFactory.create();

        messageService.favorite(message, favoritor);

        verify(userService).sendNotification(refEq(message.sender.getActual()), any(MessageFavoritedNotification.class));
    }

    @Test
    public void favoritingOwnMessageNoNotificationIsSent() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User favoritor = (User) message.sender;

        messageService.favorite(message, favoritor);

        verify(userService, never()).sendNotification(refEq(message.sender.getActual()), any(MessageFavoritedNotification.class));
    }

    @Test
    public void favoriteCorrectlyAddsUserToFavoriteSet() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User favoritor = userFactory.create();

        messageService.favorite(message, favoritor);

        assertThat(message.favorites).contains(favoritor);
    }

    @Test
    public void favoriteReturnsTrueWhenFavoriteIsAdded() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User favoritor = userFactory.create();

        boolean added = messageService.favorite(message, favoritor);

        assertThat(added).isTrue();
    }

    @Test
    public void favoriteDuplicatesAreNotAdded() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User favoritor = userFactory.create();

        messageService.favorite(message, favoritor);
        messageService.favorite(message, favoritor);

        assertThat(message.favorites).hasSize(1);
    }

    @Test
    public void favoriteReturnsFalseWhenNotAdded() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User favoritor = userFactory.create();

        messageService.favorite(message, favoritor);
        boolean added = messageService.favorite(message, favoritor);

        assertThat(added).isFalse();
    }

    @Test
    public void removeFavoriteReturnsTrueWhenFavoriteIsRemoved() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User user = userFactory.create();

        messageService.favorite(message, user);
        boolean removed = messageService.removeFavorite(message, user);

        assertThat(removed).isTrue();
    }

    @Test
    public void removeFavoriteReturnsFalseWhenFavoriteIsNotRemoved() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User user = userFactory.create();

        boolean removed = messageService.removeFavorite(message, user);

        assertThat(removed).isFalse();
    }

    @Test
    public void removeFavoriteRemovesUserFromMessageFavoritesSet() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User user = userFactory.create();

        messageService.favorite(message, user);
        messageService.removeFavorite(message, user);

        assertThat(message.favorites).hasSize(0);
        assertThat(message.favorites.contains(user)).isFalse();
    }

    @Test
    public void messageScoreIsDecrementedWhenFavoriteIsRemoved() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User user = userFactory.create();

        messageService.favorite(message, user);
        messageService.removeFavorite(message, user);

        assertThat(message.score).isZero();
    }

    @Test
    public void messageScoreIsNotDecrementedWhenFavoriteIsNotRemoved() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.FactoryTrait.WITH_PUBLIC_ROOM, MessageFactory.FactoryTrait.WITH_SENDER);
        User user = userFactory.create();

        messageService.removeFavorite(message, user);

        assertThat(message.score).isZero();
    }

    @Test
    public void flagReturnsTrueIfTheUserHasNotAlreadyFlaggedTheMessage() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        boolean didFlag = messageService.flag(message, flagger);

        assertThat(didFlag).isTrue();
    }

    @Test
    public void flagReturnsFalseIfTheUserHasAlreadyFlaggedTheMessage() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();
        messageService.flag(message, flagger);

        boolean didFlag = messageService.flag(message, flagger);

        assertThat(didFlag).isFalse();
    }

    @Test
    public void flagAddsTheUserToMessageFlags() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        messageService.flag(message, flagger);

        assertThat(message.flags).contains(flagger);
    }

    @Test
    public void flagDoesNotAddDuplicatesToMessageFlags() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        messageService.flag(message, flagger);
        messageService.flag(message, flagger);

        assertThat(message.flags).hasSize(1);
    }

    @Test
    public void removeFlagReturnsTrueIfTheUserHasFlaggedTheMessage() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        messageService.flag(message, flagger);
        boolean didRemove = messageService.removeFlag(message, flagger);

        assertThat(didRemove).isTrue();
    }

    @Test
    public void removeFlagReturnsFalseIfTheUserHasNotFlaggedTheMessage() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        boolean didRemove = messageService.removeFlag(message, flagger);

        assertThat(didRemove).isFalse();
    }

    @Test
    public void removeFlagRemovesTheUserFromMessageFlags() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        messageService.flag(message, flagger);
        messageService.removeFlag(message, flagger);

        assertThat(message.flags.contains(flagger)).isFalse();
    }
}
