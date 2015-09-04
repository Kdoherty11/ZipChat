package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
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

    public boolean senderInRoom = true;

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
    public AbstractRoom.RoomType getType() {
        return AbstractRoom.RoomType.PRIVATE;
    }

    @Override
    public boolean canEqual(Object other) {
        return other instanceof PrivateRoom;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PrivateRoom)) return false;
        PrivateRoom that = (PrivateRoom) other;
        return that.canEqual(this) && super.equals(that) &&
                Objects.equal(senderInRoom, that.senderInRoom) &&
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
        return MoreObjects.toStringHelper(this)
                .add("roomId", roomId)
                .add("senderId", AbstractUser.getId(sender))
                .add("receiverId", AbstractUser.getId(receiver))
                .add("senderInRoom", senderInRoom)
                .add("receiverInRoom", receiverInRoom)
                .add("requestId", Request.getId(request))
                .add("createdAt", createdAt)
                .add("lastActivity", lastActivity)
                .toString();
    }
}
