package models.entities;

import controllers.BaseController;
import models.ForeignEntity;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import utils.DbUtils;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Entity
public class PrivateRoom extends AbstractRoom {
    @ManyToOne
    @JoinColumn(name="sender")
    @ForeignEntity
    public User sender;

    @ManyToOne
    @JoinColumn(name="receiver")
    @ForeignEntity
    public User receiver;

    public PrivateRoom(){}

    public PrivateRoom(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public static List<PrivateRoom> getRoomsByUserId(long userId) {
        String queryString = "select p from PrivateRoom p where p.sender.id = :userId or p.receiver.id = :userId";

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
        Logger.debug("Here");
        if (User.getId(sender) == userId) {
            sender = null;
        } else if (User.getId(receiver) == userId) {
            receiver = null;
        } else {
            String error = "User with id: " + userId + " is trying to leave " + this + " but is not in it.";
            Logger.error(error);
            return error;
        }
        return BaseController.OK_STRING;
    }
}
