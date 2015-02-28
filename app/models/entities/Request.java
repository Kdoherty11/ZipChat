package models.entities;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import models.NoUpdate;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import utils.DbUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "requests")
public class Request {

    public static enum Status {
        accepted,
        denied,
        pending
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public String id;

    @NoUpdate
    @Constraints.Required
    public User fromUser;

    @NoUpdate
    @Constraints.Required
    public User toUser;

    @NoUpdate
    public String message;

    public Status status = Status.pending;

    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public long respondedTimeStamp;

    public Request() { }

    public Request(String fromUserId, String toUserId) {
        Optional<User> fromUserOptional = DbUtils.findEntityById(User.class, Preconditions.checkNotNull(fromUserId));
        if (fromUserOptional.isPresent()) {
            this.fromUser = fromUserOptional.get();
        } else {
            throw new IllegalArgumentException(DbUtils.buildEntityNotFoundString(User.ENTITY_NAME, fromUserId));
        }

        Optional<User> toUserOptional = DbUtils.findEntityById(User.class, Preconditions.checkNotNull(toUserId));
        if (toUserOptional.isPresent()) {
            this.toUser = toUserOptional.get();
        } else {
            throw new IllegalArgumentException(DbUtils.buildEntityNotFoundString(User.ENTITY_NAME, toUserId));
        }
    }

    public static List<Request> getPendingRequests(String userId) {
        String queryString = "select r from Request r where r.toUser.userId = :toUserId and status = :status";

        TypedQuery<Request> query = JPA.em().createQuery(queryString, Request.class)
                .setParameter("toUserId", userId)
                .setParameter("status", Status.pending);

        return query.getResultList();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, User.getId(toUser), User.getId(fromUser), status, message, timeStamp, respondedTimeStamp);
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
                && Objects.equal(User.getId(this.toUser), User.getId(other.toUser))
                && Objects.equal(User.getId(this.fromUser), User.getId(other.fromUser))
                && Objects.equal(this.status, other.status)
                && Objects.equal(this.message, other.message)
                && Objects.equal(this.timeStamp, other.timeStamp)
                && Objects.equal(this.respondedTimeStamp, other.respondedTimeStamp);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("toUser", User.getId(toUser))
                .add("fromUser", User.getId(fromUser))
                .add("status", status)
                .add("message", message)
                .add("timeStamp", timeStamp)
                .add("respondedTimeStamp", respondedTimeStamp)
                .toString();
    }
}
