package unit.models;

import factories.ObjectFactory;
import integration.AbstractTest;
import models.entities.Request;
import org.junit.Ignore;
import org.junit.Test;
import play.Logger;

/**
 * Created by kevin on 6/22/15.
 */
@Ignore
public class RequestTest extends AbstractTest {

//    @Test(expected = NullPointerException.class)
//    public void constructorNullSender() {
//        new Request(null, new User());
//    }
//
//    @Test(expected = NullPointerException.class)
//    public void constructorNullReceiver() {
//        new Request(new User(), null);
//    }

    @Test
    public void getPendingRequestsByReceiver() throws Throwable {
        ObjectFactory<Request> factory = new ObjectFactory<>(Request.class);
        Request request = factory.create();
        Logger.debug("Request: " + request);
    }




}
