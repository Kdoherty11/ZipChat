package models.entities;

import models.ForeignEntity;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.TypedQuery;
import java.util.List;

@Entity
public class PrivateRoom extends AbstractRoom {
    @ManyToOne
    @JoinColumn(name="sender")
    @Constraints.Required
    @ForeignEntity
    public User sender;

    @ManyToOne
    @JoinColumn(name="receiver")
    @Constraints.Required
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
}
