package models.entities;

import com.google.common.base.Objects;
import models.NoUpdate;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public String id;

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

    @NoUpdate
    public LocalDateTime timeStamp = LocalDateTime.now();

    public static List<Request> getPendingRequests(String userId) {
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

    @Override
    public int hashCode() {
        return Objects.hashCode(id, toUserId, fromUserId, status, message);
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
                && Objects.equal(this.message, other.message);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("toUserId", toUserId)
                .add("fromUserId", fromUserId)
                .add("status", status)
                .add("message", message)
                .toString();
    }
}
