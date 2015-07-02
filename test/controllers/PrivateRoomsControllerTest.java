package controllers;

import factories.ObjectFactory;
import models.entities.PrivateRoom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.GlobalSettings;
import play.mvc.Action;
import play.test.WithApplication;
import security.SecurityHelper;
import services.PrivateRoomService;

import static play.test.Helpers.fakeApplication;

/**
 * Created by kdoherty on 7/1/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class PrivateRoomsControllerTest extends WithApplication {

    private PrivateRoomsController controller;
    private ObjectFactory<PrivateRoom> privateRoomFactory;

    @Mock
    private PrivateRoomService privateRoomService;

    @Mock
    private MessagesController messagesController;

    @Mock
    private SecurityHelper securityHelper;


    @Before
    public void setUp() throws Exception {
        privateRoomFactory = new ObjectFactory<>(PrivateRoom.class);
        controller = new PrivateRoomsController(privateRoomService, messagesController,
                securityHelper);

        final GlobalSettings global = new GlobalSettings() {

            @Override
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

}
