package models.entities;

import com.google.common.base.Objects;
import controllers.BaseController;
import models.ForeignEntity;
import org.codehaus.jackson.annotate.JsonIgnore;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import utils.DbUtils;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

@Entity
public class PrivateRoom extends AbstractRoom {

    public static final long REMOVED_USER_ID = -1L;

    @ManyToOne
    @JoinColumn(name="sender")
    @ForeignEntity
    @Constraints.Required
    public User sender;

    @ManyToOne
    @JoinColumn(name="receiver")
    @ForeignEntity
    @Constraints.Required
    public User receiver;

    // These field will be set to REMOVED_USER_ID when removed removed.
    public long senderId;
    public long receiverId;

    @OneToOne (cascade=CascadeType.ALL)
    @JoinColumn(name="requestId")
    public Request request;

    public PrivateRoom(){}

    public PrivateRoom(Request request) {
        this.request = request;

        // What wil happen if request is deleted?
        this.sender = request.sender;
        this.receiver = request.receiver;

        this.senderId = sender.userId;
        this.receiverId = receiver.userId;
    }

    public static List<PrivateRoom> getRoomsByUserId(long userId) {
        String queryString = "select p from PrivateRoom p where p.senderId = :userId or p.receiverId = :userId";

        TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
                .setParameter("userId", userId);

        return query.getResultList();
    }

    public static String removeUser(long roomId, long userId) {
        Optional<PrivateRoom> roomOptional = DbUtils.findEntityById(PrivateRoom.class, roomId);

        if (roomOptional.isPresent()) {
            PrivateRoom room = roomOptional.get();

            return room.removeUser(userId);
        } else {
            return DbUtils.buildEntityNotFoundString("PrivateRoom", roomId);
        }
    }

    public String removeUser(long userId) {
        if (userId == senderId) {
            if (receiverId == REMOVED_USER_ID) {
                // Receiver left first. The request has already been set to to denied
                // when the receiver left. This receiver can't be requested by this sender again.
                JPA.em().remove(this);
            } else {
                // Sender left first. Either user can request each other.
                DbUtils.deleteEntityById(Request.class, request.id);
                senderId = REMOVED_USER_ID;
            }
        } else if (userId == receiverId) {
            if (senderId == REMOVED_USER_ID) {
                // Sender left first. Either user can still request each other.
                // Request is already deleted when the sender left
                JPA.em().remove(this);
            } else {
                // Receiver left first. The request is set to denied
                // so this receiver can't be requested by this sender again.
                request.status = Request.Status.denied;
                receiverId = REMOVED_USER_ID;
            }
        } else {
            String error = "User with id: " + userId + " is trying to leave " + this + " but is not in it.";
            Logger.error(error);
            return error;
        }

        return BaseController.OK_STRING;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("sender", sender)
                .add("receiver", Request.getId(receiver))
                .add("senderId", senderId)
                .add("receiverId", receiverId)
                .add("request", request)
                .toString();
    }
}
