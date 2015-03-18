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
    public boolean senderInRoom = true;
    public boolean receiverInRoom = true;

    @OneToOne (cascade=CascadeType.ALL)
    @JoinColumn(name="requestId")
    public Request request;

    public PrivateRoom(){}

    public PrivateRoom(Request request) {
        this.request = request;

        // Will this be ok if the request is deleted from the db?
        this.sender = request.sender;
        this.receiver = request.receiver;
    }

    public static List<PrivateRoom> getRoomsByUserId(long userId) {
        String queryString = "select p from PrivateRoom p where (p.sender.userId = :userId and p.senderInRoom = true) or (p.receiver.userId = :userId and p.receiverInRoom = true)";

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
            return DbUtils.buildEntityNotFoundString(PrivateRoom.ENTITY_NAME, roomId);
        }
    }

    public String removeUser(long userId) {
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
            String error = "User with id: " + userId + " is trying to leave " + this + " but is not in it.";
            Logger.error(error);
            return error;
        }

        // Both users can request each other again
        JPA.em().remove(request);

        return BaseController.OK_STRING;
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
}
