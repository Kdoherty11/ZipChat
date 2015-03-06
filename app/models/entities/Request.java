package models.entities;

import com.google.common.base.Objects;
import models.NoUpdate;
import org.hibernate.annotations.GenericGenerator;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Entity
@Table(name = "requests")
public class Request {

    public static enum Status {
        accepted,
        denied,
        pending
    }

    @Id
    @GenericGenerator(name = "requests_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "requests_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "requests_gen", strategy=GenerationType.SEQUENCE)
    public long id;

    @NoUpdate
    @Constraints.Required
    public String senderId;

    @NoUpdate
    @Constraints.Required
    public String receiverId;

    @NoUpdate
    public String message;

    public Status status = Status.pending;

    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public long respondedTimeStamp;

    public Request() { }

    public static List<Request> getPendingRequests(long userId) {
        String queryString = "select r from Request r where r.receiverId = :receiverId and r.status = :status";

        TypedQuery<Request> query = JPA.em().createQuery(queryString, Request.class)
                .setParameter("receiverId", userId)
                .setParameter("status", Status.pending);

        return query.getResultList();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, receiverId, senderId, status, message, timeStamp, respondedTimeStamp);
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
                && Objects.equal(this.receiverId, other.receiverId)
                && Objects.equal(this.senderId, other.senderId)
                && Objects.equal(this.status, other.status)
                && Objects.equal(this.message, other.message)
                && Objects.equal(this.timeStamp, other.timeStamp)
                && Objects.equal(this.respondedTimeStamp, other.respondedTimeStamp);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userId", id)
                .add("receiverId", receiverId)
                .add("senderId", senderId)
                .add("status", status)
                .add("message", message)
                .add("timeStamp", timeStamp)
                .add("respondedTimeStamp", respondedTimeStamp)
                .toString();
    }
}
