package integration;

import factories.ObjectFactory;
import models.entities.Device;
import models.entities.Message;
import models.entities.User;
import org.junit.Ignore;

/**
 * Created by kevin on 6/15/15.
 */
@Ignore
public class ObjectFactoryTest extends AbstractTest {

    private ObjectFactory<User> userFactory = new ObjectFactory<>(User.class);
    private ObjectFactory<Device> deviceFactory = new ObjectFactory<>(Device.class);
    private ObjectFactory<Message> messageFactory = new ObjectFactory<>(Message.class);

//    @Test
//    public void oneToManyTest() {
//        Logger.debug("STARTING OneToMany CREATE");
//        User user = userFactory.create();
//        Logger.debug("AFTER OneToMany CREATE: " + user);
//        assertEquals(user.devices).isNotNull();
//        assertEquals(user.devices).isNotEmpty();
//        assertEquals(user.devices.get(0),user);
//        //userObjectFactory.cleanUp();
//        //Logger.debug("CLEANED UP OneToMany");
//    }
//
//    @Test
//    public void manyToOneTest() {
//        Logger.debug("STARTING ManyToOne CREATE");
//        Device device = deviceFactory.create();
//        Logger.debug("AFTER ManyToOne CREATE: " + device);
//        assertEquals(device.user).isNotNull();
//        assertEquals(device.user.devices).isNotNull();
//        assertEquals(device.user.devices).isNotEmpty();
//        assertEquals(device.user.devices).containsExactly(device);
//
//        //deviceFactory.cleanUp();
//        //Logger.debug("CLEANED UP ManyToOne");
//    }
//
//    @Test
//    public void manyToManyTest() {
//        Logger.debug("STARTING ManyToMany CREATE");
//        Message message = messageFactory.create();
//        Logger.debug("AFTER ManyToMany CREATE: " + message);
//        assertEquals(message.favorites).isNotNull();
//        assertEquals(message.favorites).isNotEmpty();
//        assertEquals(message.flags).isNotNull();
//        assertEquals(message.flags).isNotEmpty();
//
//        //messageFactory.cleanUp();
//        //Logger.debug("CLEANED UP ManyToOne");
//    }

}
