package models.entities;

import com.google.common.base.Objects;
import models.ForeignEntity;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "private_rooms")
public class PrivateRoom extends AbstractRoom {

    public static final String ENTITY_NAME = "Private Room";

    @ManyToOne
    @JoinColumn(name = "sender")
    @ForeignEntity
    @Constraints.Required
    public User sender;

    @ManyToOne
    @JoinColumn(name = "receiver")
    @ForeignEntity
    @Constraints.Required
    public User receiver;

    public boolean senderInRoom = true;
    public boolean receiverInRoom = true;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "requestId")
    public Request request;

    public PrivateRoom() {
    }

    public PrivateRoom(Request request) {
        this.request = request;
        this.sender = request.sender;
        this.receiver = request.receiver;
    }

    public static List<PrivateRoom> getRoomsByUserId(long userId) {
        String queryString = "select p from PrivateRoom p where (p.sender.userId = :userId and p.senderInRoom = true) or (p.receiver.userId = :userId and p.receiverInRoom = true)";

        TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
                .setParameter("userId", userId);

        return query.getResultList();
    }

    public boolean removeUser(long userId) {
        if (userId == User.getId(sender)) {
            if (!receiverInRoom) {
                JPA.em().remove(this);
            } else {
                senderInRoom = false;
            }
        } else if (userId == User.getId(receiver)) {
            if (!senderInRoom) {
                JPA.em().remove(this);
            } else {
                receiverInRoom = false;
            }
        } else {
            return false;
        }

        // Allow both users to request each other again
        JPA.em().remove(request);
        return true;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", roomId)
                .add("senderId", User.getId(sender))
                .add("receiverId", User.getId(receiver))
                .add("senderInRoom", senderInRoom)
                .add("receiverInRoom", receiverInRoom)
                .add("request", Request.getId(request))
                .toString();
    }

    public static Optional<PrivateRoom> getRoom(long senderId, long receiverId) {

        String queryString = "select p from PrivateRoom p where " +
                "((p.sender.userId = :senderId and p.receiver.userId = :receiverId)" +
                " or (p.receiver.userId = :senderId and p.sender.userId = :receiverId))" +
                " and p.senderInRoom = true and p.receiverInRoom = true";

        TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
                .setParameter("senderId", senderId)
                .setParameter("receiverId", receiverId);

        List<PrivateRoom> rooms = query.getResultList();

        if (rooms.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(rooms.get(0));
    }
}
