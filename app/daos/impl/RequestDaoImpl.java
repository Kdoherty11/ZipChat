package daos.impl;

import daos.RequestDao;
import models.Request;
import play.db.jpa.JPA;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
public class RequestDaoImpl extends GenericDaoImpl<Request> implements RequestDao {

    public RequestDaoImpl() {
        super(Request.class);
    }

    @Override
    public List<Request> findPendingRequestsByReceiver(long receiverId) {
        String queryString = "select r from Request r where r.receiver.userId = :receiverId and r.status = :status";

        TypedQuery<Request> query = JPA.em().createQuery(queryString, Request.class)
                .setParameter("receiverId", receiverId)
                .setParameter("status", Request.Status.pending);

        return query.getResultList();
    }

    @Override
    public Optional<Request> findByUsers(long userId1, long userId2) {
        String queryString = "select r from Request r where " +
                "(r.sender.userId = :userId1 and r.receiver.userId = :userId2) " +
                "or (r.receiver.userId = :userId1 and r.sender.userId = :userId2)";

        TypedQuery<Request> query = JPA.em().createQuery(queryString, Request.class)
                .setParameter("userId1", userId1)
                .setParameter("userId2", userId2);

        List<Request> requests = query.getResultList();
        if (requests.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(requests.get(0));
        }
    }
}
