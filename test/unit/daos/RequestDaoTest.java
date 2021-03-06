package unit.daos;

import daos.RequestDao;
import daos.impl.RequestDaoImpl;
import factories.FieldOverride;
import factories.RequestFactory;
import models.Request;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by kdoherty on 7/30/15.
 */
public class RequestDaoTest extends AbstractDaoTest {

    private RequestDao requestDao;
    private RequestFactory requestFactory;

    @Before
    public void setUp() {
        requestDao = new RequestDaoImpl();
        requestFactory = new RequestFactory();
    }

    @Test
    public void findPendingRequestsByReceiverReturnsEmptyListIfNoneFound() {
        JPA.withTransaction(() -> {
            List<Request> requests = requestDao.findPendingRequestsByReceiver(1);
            assertTrue(requests.isEmpty());
        });
    }

    @Test
    public void findPendingRequestsByReceiverReturnsEmptyWhenNonPendingStatus() throws InstantiationException, IllegalAccessException {
        JPA.withTransaction(() -> {
            Request request = requestFactory.create(
                    RequestFactory.Trait.WITH_PERSISTED_SENDER_AND_RECEIVER,
                    FieldOverride.of("status", Request.Status.accepted),
                    RequestFactory.Trait.PERSISTED);
            List<Request> requests = requestDao.findPendingRequestsByReceiver(request.receiver.userId);
            assertTrue(requests.isEmpty());
        });
    }

    @Test
    public void findPendingRequestsByReceiverReturnsNonEmpty() throws InstantiationException, IllegalAccessException {
        JPA.withTransaction(() -> {
            Request request = requestFactory.create(RequestFactory.Trait.PERSISTED_WITH_SENDER_AND_RECEIVER);
            List<Request> requests = requestDao.findPendingRequestsByReceiver(request.receiver.userId);
            assertFalse(requests.isEmpty());
            assertTrue(requests.contains(request));
        });
    }

    @Test
    public void findBySenderAndReceiverReturnsEmptyWhenRequestDoesntExist() {
        JPA.withTransaction(() -> {
            long senderId = 100;
            long receiverId = 150;
            Optional<Request> optional = requestDao.findByUsers(150, 200);
            assertFalse("A Request with senderId " + senderId + " and receiverId " + receiverId + " should not be present", optional.isPresent());
        });
    }

    @Test
    public void findBySenderAndReceiverReturnsRequestWhenSwitched() throws InstantiationException, IllegalAccessException {
        JPA.withTransaction(() -> {
            Request request = requestFactory.create(RequestFactory.Trait.PERSISTED_WITH_SENDER_AND_RECEIVER);
            Optional<Request> optional = requestDao.findByUsers(request.receiver.userId, request.sender.userId);
            assertTrue(optional.isPresent());
        });
    }

    @Test
    public void findBySenderAndReceiverReturnsNonEmpty() throws InstantiationException, IllegalAccessException {
        JPA.withTransaction(() -> {
            Request request = requestFactory.create(RequestFactory.Trait.PERSISTED_WITH_SENDER_AND_RECEIVER);
            Optional<Request> optional = requestDao.findByUsers(request.sender.userId, request.receiver.userId);
            assertTrue(optional.isPresent());
            assertEquals(request, optional.get());
        });
    }
}
