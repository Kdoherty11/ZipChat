package models.entities;

import com.google.common.base.*;
import com.google.common.base.Objects;
import notifications.ChatResponseNotification;
import org.hibernate.annotations.GenericGenerator;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.Optional;

@Entity
@Table(name = "requests")
public class Request {

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
    @JoinColumn(name="sender")
    @Constraints.Required
    public User sender;

    @ManyToOne
    @JoinColumn(name="receiver")
    @Constraints.Required
    public User receiver;

    public Status status = Status.pending;

    public long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public long respondedTimeStamp;

    public Request() {
        // Needed for JPA
    }

    public Request(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }


    @SuppressWarnings("unused")
    public String validate() {
        // Prevents duplicate requests between 2 users
        if (getRequest(sender.userId, receiver.userId).isPresent()) {
            return "A request with sender " + sender.userId + " and receiver " + receiver.userId + " already exists";
        }

        return null;
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

        sender.sendNotification(new ChatResponseNotification(this, status));

        if (status == Status.accepted) {
            PrivateRoom room = new PrivateRoom(this);
            JPA.em().persist(room);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equal(createdAt, request.createdAt) &&
                Objects.equal(respondedTimeStamp, request.respondedTimeStamp) &&
                Objects.equal(sender, request.sender) &&
                Objects.equal(receiver, request.receiver) &&
                Objects.equal(status, request.status);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sender, receiver, status, createdAt, respondedTimeStamp);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("requestId", requestId)
                .add("sender", sender.userId)
                .add("receiver", receiver.userId)
                .add("status", status)
                .add("createdAt", createdAt)
                .add("respondedTimeStamp", respondedTimeStamp)
                .toString();
    }
}
