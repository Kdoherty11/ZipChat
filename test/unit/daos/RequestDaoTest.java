package unit.daos;

import daos.RequestDao;
import daos.impl.RequestDaoImpl;
import factories.PropOverride;
import factories.RequestFactory;
import models.entities.Request;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import play.test.WithApplication;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by kdoherty on 7/30/15.
 */
public class RequestDaoTest extends WithApplication {

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
                    PropOverride.of("status", Request.Status.accepted),
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
            Optional<Request> optional = requestDao.findBySenderAndReceiver(1, 2);
            assertFalse(optional.isPresent());
        });
    }

    @Test
    public void findBySenderAndReceiverReturnsEmptyWhenSwitched() throws InstantiationException, IllegalAccessException {
        JPA.withTransaction(() -> {
            Request request = requestFactory.create(RequestFactory.Trait.PERSISTED_WITH_SENDER_AND_RECEIVER);
            Optional<Request> optional = requestDao.findBySenderAndReceiver(request.receiver.userId, request.sender.userId);
            assertFalse(optional.isPresent());
        });
    }

    @Test
    public void findBySenderAndReceiverReturnsNonEmpty() throws InstantiationException, IllegalAccessException {
        JPA.withTransaction(() -> {
            Request request = requestFactory.create(RequestFactory.Trait.PERSISTED_WITH_SENDER_AND_RECEIVER);
            Optional<Request> optional = requestDao.findBySenderAndReceiver(request.sender.userId, request.receiver.userId);
            assertTrue(optional.isPresent());
            assertEquals(request, optional.get());
        });
    }
}
