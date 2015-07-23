package daos.impl;

import daos.RequestDao;
import models.entities.Request;
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
    public Optional<Request> findBySenderAndReceiver(long senderId, long receiverId) {
        String queryString = "select r from Request r where r.sender.userId = :senderId and r.receiver.userId = :receiverId";

        TypedQuery<Request> query = JPA.em().createQuery(queryString, Request.class)
                .setParameter("senderId", senderId)
                .setParameter("receiverId", receiverId);

        List<Request> requests = query.getResultList();
        if (requests.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(requests.get(0));
        }
    }
}
