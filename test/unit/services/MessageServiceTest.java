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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.*;

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
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User favoritor = userFactory.create();
        messageService.favorite(message, favoritor);

        assertEquals(1, message.score);
    }

    @Test
    public void favoriteSendsNotificationToMessageSender() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User favoritor = userFactory.create();

        messageService.favorite(message, favoritor);

        verify(userService).sendNotification(refEq((User) message.sender), any(MessageFavoritedNotification.class));
    }

    @Test
    public void favoriteSendsNotificationToAnonSender() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_ANON_SENDER);
        User favoritor = userFactory.create();

        messageService.favorite(message, favoritor);

        verify(userService).sendNotification(refEq(message.sender.getActual()), any(MessageFavoritedNotification.class));
    }

    @Test
    public void favoritingOwnMessageNoNotificationIsSent() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User favoritor = (User) message.sender;

        messageService.favorite(message, favoritor);

        verify(userService, never()).sendNotification(refEq(message.sender.getActual()), any(MessageFavoritedNotification.class));
    }

    @Test
    public void favoriteCorrectlyAddsUserToFavoriteSet() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User favoritor = userFactory.create();

        messageService.favorite(message, favoritor);

        assertTrue(message.favorites.contains(favoritor));
    }

    @Test
    public void favoriteReturnsTrueWhenFavoriteIsAdded() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User favoritor = userFactory.create();

        boolean added = messageService.favorite(message, favoritor);

        assertTrue(added);
    }

    @Test
    public void favoriteDuplicatesAreNotAdded() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User favoritor = userFactory.create();

        messageService.favorite(message, favoritor);
        messageService.favorite(message, favoritor);

        assertEquals(1, message.favorites.size());
    }

    @Test
    public void favoriteReturnsFalseWhenNotAdded() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User favoritor = userFactory.create();

        messageService.favorite(message, favoritor);
        boolean added = messageService.favorite(message, favoritor);

        assertFalse(added);
    }

    @Test
    public void removeFavoriteReturnsTrueWhenFavoriteIsRemoved() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User user = userFactory.create();

        messageService.favorite(message, user);
        boolean removed = messageService.removeFavorite(message, user);

        assertTrue(removed);
    }

    @Test
    public void removeFavoriteReturnsFalseWhenFavoriteIsNotRemoved() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User user = userFactory.create();

        boolean removed = messageService.removeFavorite(message, user);

        assertFalse(removed);
    }

    @Test
    public void removeFavoriteRemovesUserFromMessageFavoritesSet() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User user = userFactory.create();

        messageService.favorite(message, user);
        messageService.removeFavorite(message, user);

        assertTrue(message.favorites.isEmpty());
    }

    @Test
    public void messageScoreIsDecrementedWhenFavoriteIsRemoved() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User user = userFactory.create();

        messageService.favorite(message, user);
        messageService.removeFavorite(message, user);

        assertEquals(0, message.score);
    }

    @Test
    public void messageScoreIsNotDecrementedWhenFavoriteIsNotRemoved() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create(
                MessageFactory.Trait.WITH_PUBLIC_ROOM, MessageFactory.Trait.WITH_SENDER);
        User user = userFactory.create();

        messageService.removeFavorite(message, user);

        assertEquals(0, message.score);
    }

    @Test
    public void flagReturnsTrueIfTheUserHasNotAlreadyFlaggedTheMessage() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        boolean didFlag = messageService.flag(message, flagger);

        assertTrue(didFlag);
    }

    @Test
    public void flagReturnsFalseIfTheUserHasAlreadyFlaggedTheMessage() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();
        messageService.flag(message, flagger);

        boolean didFlag = messageService.flag(message, flagger);

        assertFalse(didFlag);
    }

    @Test
    public void flagAddsTheUserToMessageFlags() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        messageService.flag(message, flagger);

        assertTrue(message.flags.contains(flagger));
    }

    @Test
    public void flagDoesNotAddDuplicatesToMessageFlags() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        messageService.flag(message, flagger);
        messageService.flag(message, flagger);

        assertEquals(1, message.flags.size());
    }

    @Test
    public void removeFlagReturnsTrueIfTheUserHasFlaggedTheMessage() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        messageService.flag(message, flagger);
        boolean didRemove = messageService.removeFlag(message, flagger);

        assertTrue(didRemove);
    }

    @Test
    public void removeFlagReturnsFalseIfTheUserHasNotFlaggedTheMessage() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        boolean didRemove = messageService.removeFlag(message, flagger);

        assertFalse(didRemove);
    }

    @Test
    public void removeFlagRemovesTheUserFromMessageFlags() throws InstantiationException, IllegalAccessException {
        Message message = messageFactory.create();
        User flagger = userFactory.create();

        messageService.flag(message, flagger);
        messageService.removeFlag(message, flagger);

        assertFalse(message.flags.contains(flagger));
    }

    @Test
    public void getMessages() {
        long roomId = 1;
        int limit = 25;
        int offset = 0;
        List<Message> expected = new ArrayList<>();
        when(messageDao.getMessages(roomId, limit, offset)).thenReturn(expected);

        List<Message> actual = messageService.getMessages(roomId, limit, offset);

        assertSame(expected, actual);
    }


}
