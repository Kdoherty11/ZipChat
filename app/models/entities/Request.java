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
    @GeneratedValue(generator = "requests_gen", strategy = GenerationType.SEQUENCE)
    public long requestId;

    @ManyToOne
    @JoinColumn(name = "sender")
    @Constraints.Required
    public User sender;

    @ManyToOne
    @JoinColumn(name = "receiver")
    @Constraints.Required
    public User receiver;

    public Status status = Status.pending;

    public long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public long respondedTimeStamp;

    public Request() {
        // Needed for JPA
    }

    public Request(User sender, User receiver) {
        this.sender = Preconditions.checkNotNull(sender);
        this.receiver = Preconditions.checkNotNull(receiver);
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
                .add("sender", AbstractUser.getId(sender))
                .add("receiver", AbstractUser.getId(receiver))
                .add("status", status)
                .add("createdAt", createdAt)
                .add("respondedTimeStamp", respondedTimeStamp)
                .toString();
    }
}
