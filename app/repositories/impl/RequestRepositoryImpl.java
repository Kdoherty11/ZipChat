package repositories.impl;

import models.entities.PrivateRoom;
import models.entities.Request;
import play.db.jpa.JPA;
import repositories.RequestRepository;
import utils.DbUtils;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
public class RequestRepositoryImpl implements RequestRepository {

    @Override
    public Optional<Request> findById(long requestId) {
        return DbUtils.findEntityById(Request.class, requestId);
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
    public String getStatus(long senderId, long receiverId) {

        Optional<PrivateRoom> privateRoomOptional = PrivateRoom.getRoom(senderId, receiverId);

        if (privateRoomOptional.isPresent()) {
            return Long.toString(privateRoomOptional.get().roomId);
        }

        Optional<Request> requestOptional = findBySenderAndReceiver(senderId, receiverId);
        if (requestOptional.isPresent()) {
            return requestOptional.get().status.name();
        } else {
            return "none";
        }
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

    @Override
    public void save(Request request) {
        JPA.em().persist(request);
    }
}
