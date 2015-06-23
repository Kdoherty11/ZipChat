package integration;

import factories.ObjectFactory;
import models.entities.Device;
import models.entities.Message;
import models.entities.User;

/**
 * Created by kevin on 6/15/15.
 */
public class ObjectFactoryTest extends AbstractTest {

    private ObjectFactory<User> userFactory = new ObjectFactory<>(User.class);
    private ObjectFactory<Device> deviceFactory = new ObjectFactory<>(Device.class);
    private ObjectFactory<Message> messageFactory = new ObjectFactory<>(Message.class);

//    @Test
//    public void oneToManyTest() {
//        Logger.debug("STARTING OneToMany CREATE");
//        User user = userFactory.create();
//        Logger.debug("AFTER OneToMany CREATE: " + user);
//        assertThat(user.devices).isNotNull();
//        assertThat(user.devices).isNotEmpty();
//        assertThat(user.devices.get(0)).isEqualTo(user);
//        //userObjectFactory.cleanUp();
//        //Logger.debug("CLEANED UP OneToMany");
//    }
//
//    @Test
//    public void manyToOneTest() {
//        Logger.debug("STARTING ManyToOne CREATE");
//        Device device = deviceFactory.create();
//        Logger.debug("AFTER ManyToOne CREATE: " + device);
//        assertThat(device.user).isNotNull();
//        assertThat(device.user.devices).isNotNull();
//        assertThat(device.user.devices).isNotEmpty();
//        assertThat(device.user.devices).containsExactly(device);
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
//        assertThat(message.favorites).isNotNull();
//        assertThat(message.favorites).isNotEmpty();
//        assertThat(message.flags).isNotNull();
//        assertThat(message.flags).isNotEmpty();
//
//        //messageFactory.cleanUp();
//        //Logger.debug("CLEANED UP ManyToOne");
//    }

}
