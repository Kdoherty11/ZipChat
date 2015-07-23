package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import play.data.validation.Constraints;

import javax.persistence.*;

@Entity
@Table(name = "private_rooms")
public class PrivateRoom extends AbstractRoom {

    @ManyToOne
    @JoinColumn(name = "sender")
    @Constraints.Required
    public User sender;

    @ManyToOne
    @JoinColumn(name = "receiver")
    @Constraints.Required
    public User receiver;

    @JsonIgnore
    public boolean senderInRoom = true;

    @JsonIgnore
    public boolean receiverInRoom = true;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "requestId")
    public Request request;

    public PrivateRoom() {
        // Needed for JPA
    }

    public PrivateRoom(Request request) {
        this.request = request;
        this.sender = request.sender;
        this.receiver = request.receiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrivateRoom that = (PrivateRoom) o;
        return Objects.equal(senderInRoom, that.senderInRoom) &&
                Objects.equal(receiverInRoom, that.receiverInRoom) &&
                Objects.equal(sender, that.sender) &&
                Objects.equal(receiver, that.receiver) &&
                Objects.equal(request, that.request);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), sender, receiver, senderInRoom, receiverInRoom, request);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("senderId", AbstractUser.getId(sender))
                .add("receiverId", AbstractUser.getId(receiver))
                .add("senderInRoom", senderInRoom)
                .add("receiverInRoom", receiverInRoom)
                .add("requestId", request == null ? -1 : request.requestId)
                .toString();
    }
}
