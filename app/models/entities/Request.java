package models.entities;

import com.google.common.base.Objects;
import models.ForeignEntity;
import models.NoUpdate;
import org.hibernate.annotations.GenericGenerator;
import play.Logger;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.db.jpa.JPA;
import utils.NotificationUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Entity
@Table(name = "requests")
public class Request {

    public enum Status {
        accepted,
        denied,
        pending
    }

    public static final String ENTITY_NAME = "Request";

    @Id
    @GenericGenerator(name = "requests_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "requests_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "requests_gen", strategy=GenerationType.SEQUENCE)
    public long id;

    @ManyToOne
    @NoUpdate
    @ForeignEntity
    @JoinColumn(name="sender")
    @Constraints.Required
    public User sender;

    @ManyToOne
    @ForeignEntity
    @JoinColumn(name="receiver")
    @Constraints.Required
    public User receiver;

    @NoUpdate
    public String message;

    public Status status = Status.pending;

    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public long respondedTimeStamp;

    public Request() { }


    @SuppressWarnings("unused")
    public String validate() {
        if (Request.getRequest(User.getId(sender), User.getId(receiver)).isPresent()) {
            return "A request with sender " + User.getId(sender) + " and receiver " + User.getId(receiver) + " already exists";
        }

        return null;
    }

    public static long getId(Request request) {
        return request == null ? -1 : request.id;
    }

    public static List<Request> getPendingRequestsByReceiver(long receiverId) {
        String queryString = "select r from Request r where r.receiver.id = :receiverId and r.status = :status";

        TypedQuery<Request> query = JPA.em().createQuery(queryString, Request.class)
                .setParameter("receiverId", receiverId)
                .setParameter("status", Status.pending);

        return query.getResultList();
    }

    public static String getStatus(long senderId, long receiverId) {
        String queryString = "select r.status from Request r where r.sender.id = :senderId and r.receiver.id = :receiverId";

        TypedQuery<Status> query = JPA.em().createQuery(queryString, Status.class)
                .setParameter("senderId", senderId)
                .setParameter("receiverId", receiverId);

        List<Status> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return "none";
        } else {
            return resultList.get(0).toString();
        }
    }

    public static Optional<Request> getRequest(long senderId, long receiverId) {
        String queryString = "select r from Request r where r.sender.id = :senderId and r.receiver.id = :receiverId";

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

    public void handleResponse(Status status) {
        this.status = status;
        this.respondedTimeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        NotificationUtils.sendChatResponse(receiver, sender, status);

        if (status == Status.accepted) {
            PrivateRoom room = new PrivateRoom(this);
            JPA.em().persist(room);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, User.getId(receiver), User.getId(sender), status, message, timeStamp, respondedTimeStamp);
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
                && Objects.equal(User.getId(this.receiver), User.getId(other.receiver))
                && Objects.equal(User.getId(this.sender), User.getId(other.sender))
                && Objects.equal(this.status, other.status)
                && Objects.equal(this.message, other.message)
                && Objects.equal(this.timeStamp, other.timeStamp)
                && Objects.equal(this.respondedTimeStamp, other.respondedTimeStamp);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userId", id)
                .add("receiverId", receiver.userId)
                .add("senderId", sender.userId)
                .add("status", status)
                .add("message", message)
                .add("timeStamp", timeStamp)
                .add("respondedTimeStamp", respondedTimeStamp)
                .toString();
    }
}
