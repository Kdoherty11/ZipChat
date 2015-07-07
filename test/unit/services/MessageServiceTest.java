package unit.services;

import daos.MessageDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.AbstractUserService;
import services.MessageService;
import services.impl.MessageServiceImpl;

/**
 * Created by kdoherty on 7/6/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageServiceTest {

    private MessageService messageService;

    @Mock
    private MessageDao messageDao;

    @Mock
    private AbstractUserService abstractUserService;

    public void setUp() {
        messageService = new MessageServiceImpl(messageDao, abstractUserService);
    }

    @Test
    public void favoriteTrue() {

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
