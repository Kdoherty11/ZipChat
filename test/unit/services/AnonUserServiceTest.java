package unit.services;

import daos.AnonUserDao;
import factories.AnonUserFactory;
import factories.PublicRoomFactory;
import factories.UserFactory;
import models.AnonUser;
import models.PublicRoom;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.AnonUserService;
import services.impl.AnonUserServiceImpl;
import utils.TestUtils;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 7/7/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class AnonUserServiceTest {

    private AnonUserService anonUserService;

    @Mock
    private AnonUserDao anonUserDao;

    @Mock
    private Random random;

    private PublicRoomFactory publicRoomFactory;
    private UserFactory userFactory;
    private AnonUserFactory anonUserFactory;

    @Before
    public void setUp() {
        anonUserService = spy(new AnonUserServiceImpl(anonUserDao, random));
        publicRoomFactory = new PublicRoomFactory();
        userFactory = new UserFactory();
        anonUserFactory = new AnonUserFactory();
    }

    @Test
    public void getOrCreateAnonUserUsesExistingAnonUserIfExists() {
        User mockActual = mock(User.class);
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(anonUserService.getAnonUser(mockActual, mockRoom)).thenReturn(Optional.empty());
        AnonUser mockAnonUser = mock(AnonUser.class);
        Optional<AnonUser> existing = Optional.of(mockAnonUser);
        when(anonUserService.getAnonUser(mockActual, mockRoom)).thenReturn(existing);

        AnonUser result = anonUserService.getOrCreateAnonUser(mockActual, mockRoom);

        assertEquals(mockAnonUser, result);
    }

    @Test
    public void getOrCreateAnonUserCreatesANewAnonUserIfNoneExist() {
        User mockActual = mock(User.class);
        PublicRoom mockRoom = mock(PublicRoom.class);
        when(anonUserService.getAnonUser(mockActual, mockRoom)).thenReturn(Optional.empty());
        Optional<AnonUser> existing = Optional.empty();
        when(anonUserService.getAnonUser(mockActual, mockRoom)).thenReturn(existing);

        AnonUser result = anonUserService.getOrCreateAnonUser(mockActual, mockRoom);

        assertNotNull(result);
        verify(anonUserDao).save(any(AnonUser.class));
    }

    @Test
    public void getOrCreateUserCreatesAnAnonUserWithAnUnusedAlias() throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        PublicRoom room = publicRoomFactory.create();
        User user = userFactory.create();
        when(anonUserService.getAnonUser(user, room)).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked") Set<String> fullNames =
                (Set<String>) TestUtils.getHiddenField(AnonUserServiceImpl.class, "FULL_NAMES");
        int numPossibleAliasNames = fullNames.size();
        int numCreatedAnonUsers = numPossibleAliasNames - 1;
        List<AnonUser> anonUsers = anonUserFactory.createList(numCreatedAnonUsers);
        Set<String> usedAliases = new HashSet<>();
        int anonUsersIndex = 0;
        for (String alias : fullNames) {
            if (numCreatedAnonUsers == anonUsersIndex) {
                break;
            }
            anonUsers.get(anonUsersIndex++).name = alias;
            usedAliases.add(alias);
        }
        room.anonUsers = anonUsers;

        AnonUser anonUser = anonUserService.getOrCreateAnonUser(user, room);

        assertFalse(usedAliases.contains(anonUser.name));
    }

    @Test
    public void getOrCreateAnonUsersThrowsAnExceptionIfNoMoreAliasesExist() throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        PublicRoom room = publicRoomFactory.create();
        User user = userFactory.create();
        when(anonUserService.getAnonUser(user, room)).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked") Set<String> fullNames =
                (Set<String>) TestUtils.getHiddenField(AnonUserServiceImpl.class, "FULL_NAMES");
        int numPossibleAliasNames = fullNames.size();
        List<AnonUser> anonUsers = anonUserFactory.createList(numPossibleAliasNames);
        int anonUsersIndex = 0;
        for (String alias : fullNames) {
            anonUsers.get(anonUsersIndex++).name = alias;
        }
        room.anonUsers = anonUsers;

        try {
            anonUserService.getOrCreateAnonUser(user, room);
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("There are no more available aliases"));
        }

    }

    @Test
    public void getOrCreateAnonUsersImpossiblyThrowsAnException() throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        PublicRoom room = mock(PublicRoom.class);
        User user = mock(User.class);
        when(anonUserService.getAnonUser(user, room)).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked") Set<String> fullNames =
                (Set<String>) TestUtils.getHiddenField(AnonUserServiceImpl.class, "FULL_NAMES");
        int numPossibleAliasNames = fullNames.size();
        when(room.anonUsers).thenReturn(Collections.emptyList());
        when(random.nextInt(numPossibleAliasNames)).thenReturn(numPossibleAliasNames + 1);

        try {
            anonUserService.getOrCreateAnonUser(user, room);
            fail();
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Random index was not in set... Should never get here"));
        }
    }



}
