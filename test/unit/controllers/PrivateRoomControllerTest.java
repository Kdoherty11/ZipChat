package unit.controllers;

import controllers.PrivateRoomsController;
import factories.MessageFactory;
import factories.PrivateRoomFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.GlobalSettings;
import play.mvc.Action;
import services.MessageService;
import services.PrivateRoomService;
import services.UserService;
import services.impl.SecurityServiceImpl;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.start;

/**
 * Created by kdoherty on 7/9/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class PrivateRoomControllerTest {

    private PrivateRoomsController controller;
    private PrivateRoomFactory privateRoomFactory;
    private MessageFactory messageFactory;

    @Mock
    private PrivateRoomService privateRoomService;

    @Mock
    private MessageService messageService;

    @Mock
    private SecurityServiceImpl securityServiceImpl;

    @Mock
    private UserService userService;

    @Before
    public void setUp() throws Exception {
        privateRoomFactory = new PrivateRoomFactory();
        messageFactory = new MessageFactory();
        controller = new PrivateRoomsController(privateRoomService, messageService,
                securityServiceImpl);

        final GlobalSettings global = new GlobalSettings() {

            @Override
            @SuppressWarnings("unchecked")
            public <T> T getControllerInstance(Class<T> clazz) {
                if (clazz.getSuperclass() == Action.class) {
                    return null;
                }

                return (T) controller;
            }

        };

        start(fakeApplication(global));
    }

    @Test
    public void getRoomsByUserId() {

    }

    @Test
    public void getRoomsByUserIdIsSecured() {

    }

    @Test
    public void leaveRoom() {

    }

    @Test
    public void leaveRoomRoomNotFound() {

    }

    @Test
    public void leaveRoomUserNotInRoom() {

    }

    @Test
    public void leaveRoomIsSecured() {

    }

    @Test
    public void joinRoomRoomNotFound() {

    }

    @Test
    public void joinRoomIsSecured() {

    }

    @Test
    public void getMessagesRoomNotFound() {

    }

    @Test
    public void getMessagesWithNegativeOffset() {

    }

    @Test
    public void getMessagesWithNegativeLimit() {

    }

    @Test
    public void getMessagesWorksWithZeroLimitAndZeroOffset() throws IllegalAccessException, InstantiationException {

    }

}
