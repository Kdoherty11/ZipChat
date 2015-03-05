package models.entities;

import com.google.common.base.Objects;
import models.NoUpdate;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Entity
@Table(name = "requests")
@SequenceGenerator(name="requests_id_seq", sequenceName="requests_id_seq", allocationSize=1)
public class Request {

    public static enum Status {
        accepted,
        denied,
        pending
    }

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="requests_id_seq")
    @Column(name = "id")
    public long id;

    @NoUpdate
    @Constraints.Required
    public String fromUserId;

    @NoUpdate
    @Constraints.Required
    public String toUserId;

    @NoUpdate
    public String message;

    public Status status = Status.pending;

    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public long respondedTimeStamp;

    public Request() { }

    public static List<Request> getPendingRequests(long userId) {
        String queryString = "select r from Request r where r.toUserId = :toUserId and r.status = :status";

        TypedQuery<Request> query = JPA.em().createQuery(queryString, Request.class)
                .setParameter("toUserId", userId)
                .setParameter("status", Status.pending);

        return query.getResultList();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, toUserId, fromUserId, status, message, timeStamp, respondedTimeStamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Request other = (Request) obj;
        return Objects.equal(this.id, other.id)
                && Objects.equal(this.toUserId, other.toUserId)
                && Objects.equal(this.fromUserId, other.fromUserId)
                && Objects.equal(this.status, other.status)
                && Objects.equal(this.message, other.message)
                && Objects.equal(this.timeStamp, other.timeStamp)
                && Objects.equal(this.respondedTimeStamp, other.respondedTimeStamp);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userId", id)
                .add("toUserId", toUserId)
                .add("fromUserId", fromUserId)
                .add("status", status)
                .add("message", message)
                .add("timeStamp", timeStamp)
                .add("respondedTimeStamp", respondedTimeStamp)
                .toString();
    }
}
