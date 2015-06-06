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

    public static final String ENTITY_NAME = "Request";

    public enum Status {
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
    public long requestId;

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

    public Status status = Status.pending;

    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public long respondedTimeStamp;

    public Request() { }


    @SuppressWarnings("unused")
    public String validate() {
        // Prevents duplicate requests between 2 users
        if (getRequest(User.getId(sender), User.getId(receiver)).isPresent()) {
            return "A request with sender " + User.getId(sender) + " and receiver " + User.getId(receiver) + " already exists";
        }

        return null;
    }

    public static long getId(Request request) {
        return request == null ? -1 : request.requestId;
    }

    public static List<Request> getPendingRequestsByReceiver(long receiverId) {
        String queryString = "select r from Request r where r.receiver.userId = :receiverId and r.status = :status";

        TypedQuery<Request> query = JPA.em().createQuery(queryString, Request.class)
                .setParameter("receiverId", receiverId)
                .setParameter("status", Status.pending);

        return query.getResultList();
    }

    public static String getStatus(long senderId, long receiverId) {

        Optional<PrivateRoom> privateRoomOptional = PrivateRoom.getRoom(senderId, receiverId);

        if (privateRoomOptional.isPresent()) {
            return Long.toString(privateRoomOptional.get().roomId);
        }

        Optional<Request> requestOptional = getRequest(senderId, receiverId);
        if (requestOptional.isPresent()) {
            return requestOptional.get().status.name();
        } else {
            return "none";
        }
    }

    public static Optional<Request> getRequest(long senderId, long receiverId) {
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
        return Objects.hashCode(requestId, User.getId(receiver), User.getId(sender), status, timeStamp, respondedTimeStamp);
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
        return Objects.equal(this.requestId, other.requestId)
                && Objects.equal(User.getId(this.receiver), User.getId(other.receiver))
                && Objects.equal(User.getId(this.sender), User.getId(other.sender))
                && Objects.equal(this.status, other.status)
                && Objects.equal(this.timeStamp, other.timeStamp)
                && Objects.equal(this.respondedTimeStamp, other.respondedTimeStamp);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("requestId", requestId)
                .add("receiverId", receiver.userId)
                .add("senderId", sender.userId)
                .add("status", status)
                .add("timeStamp", timeStamp)
                .add("respondedTimeStamp", respondedTimeStamp)
                .toString();
    }
}
