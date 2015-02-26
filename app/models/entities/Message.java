package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.jpa.Transactional;
import utils.DbUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.Optional;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public String id;

    @Constraints.Required
    public String message;

    @ManyToOne
    @JoinColumn(name="roomId")
    public Room room;

    @ManyToOne
    @JoinColumn(name="userId")
    @Constraints.Required
    public User user;

    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date timeStamp = new Date();

    public Message() { }

    public Message(String roomId, User user, String message) {
        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);
        if (roomOptional.isPresent()) {
            roomOptional.get().addMessage(this);
        } else {
           throw new IllegalArgumentException(DbUtils.buildEntityNotFoundString("Room", roomId));
        }


        this.user = user;
        this.message = message;
    }

    @Transactional
    public void addToRoom() {
        if (room != null) {
            room.addMessage(this);
        } else {
            Logger.error("Message: " + this + " has a null room");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, message, room, user, timeStamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Message other = (Message) obj;
        return Objects.equal(this.id, other.id)
                && Objects.equal(this.message, other.message)
                && Objects.equal(this.room, other.room)
                && Objects.equal(this.user, other.user)
                && Objects.equal(this.timeStamp, other.timeStamp);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("message", message)
                .add("roomId", room)
                .add("user", user)
                .add("timeStamp", timeStamp)
                .toString();
    }
}
