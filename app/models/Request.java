package models;

import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Constraints.Required
    @NoUpdate
    public long toUserId;

    @Constraints.Required
    @NoUpdate
    public long fromUserId;

    @Constraints.Required
    public Status status = Status.pending;

    @NoUpdate
    public String message;

    public static List<Request> getPendingRequests(long userId) {
        String queryString = "select r from Request r where r.toUserId = :toUserId and status = :status";

        Query query = JPA.em().createQuery(queryString)
                .setParameter("toUserId", userId)
                .setParameter("status", Status.pending);

        return query.getResultList();
    }

    public static enum Status {
        accepted,
        denied,
        pending
    }
}
