package models.entities;

import com.google.common.base.Objects;
import models.NoUpdate;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.Transactional;
import utils.DbUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    @Constraints.Required
    public String userId;

    @NoUpdate
    public LocalDateTime timeStamp = LocalDateTime.now();

    public Message() { }

    public Message(String roomId, String userId, String message) {
        Optional<Room> roomOptional = DbUtils.findEntityById(Room.class, roomId);
        if (roomOptional.isPresent()) {
            this.room = roomOptional.get();
        } else {
           throw new IllegalArgumentException(DbUtils.buildEntityNotFoundString("Room", roomId));
        }

        Optional<User> userOptional = DbUtils.findEntityById(User.class, roomId);
        if (userOptional.isPresent()) {
            this.userId = userId;
        } else {
            throw new IllegalArgumentException(DbUtils.buildEntityNotFoundString("User", userId));
        }

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
        return Objects.hashCode(id, message, room, userId, timeStamp);
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
                && Objects.equal(this.room.roomId, other.room.roomId)
                && Objects.equal(this.userId, other.userId)
                && Objects.equal(this.timeStamp, other.timeStamp);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("message", message)
                .add("roomId", room)
                .add("userId", userId)
                .add("timeStamp", timeStamp)
                .toString();
    }
}
