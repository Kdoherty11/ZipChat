package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import models.ForeignEntity;
import org.hibernate.annotations.GenericGenerator;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.Transactional;
import utils.DbUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GenericGenerator(name = "messages_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "messages_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "messages_gen", strategy=GenerationType.SEQUENCE)
    @JsonIgnore
    public long id;

    @Constraints.Required
    public String message;

    @ManyToOne
    @JoinColumn(name="roomId")
    @JsonIgnore
    public AbstractRoom room;

    @ManyToOne
    @JoinColumn(name="userId")
    @Constraints.Required
    @ForeignEntity
    public User sender;

    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public Message() { }

    public Message(long roomId, long userId, String message) {
        Optional<AbstractRoom> roomOptional = DbUtils.findEntityById(AbstractRoom.class, roomId);
        if (roomOptional.isPresent()) {
            this.room = roomOptional.get();
        } else {
           throw new IllegalArgumentException(DbUtils.buildEntityNotFoundString(AbstractRoom.class.getSimpleName(), roomId));
        }
        setUserById(userId);
        this.message = Preconditions.checkNotNull(message);
    }

    public Message(AbstractRoom room, long userId, String message) {
        this.room = Preconditions.checkNotNull(room);
        setUserById(userId);
        this.message = Preconditions.checkNotNull(message);
    }

    private void setUserById(long userId) {
        Optional<User> userOptional = DbUtils.findEntityById(User.class, Preconditions.checkNotNull(userId));
        if (userOptional.isPresent()) {
            this.sender = userOptional.get();
        } else {
            throw new IllegalArgumentException(DbUtils.buildEntityNotFoundString(User.ENTITY_NAME, userId));
        }
    }

    @Transactional
    public void addToRoom() {
        if (room != null) {
            room.addMessage(this);
        } else {
            Logger.error(this + " has a null room");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, message, AbstractRoom.getId(room), User.getId(sender), timeStamp);
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
                && Objects.equal(AbstractRoom.getId(this.room), AbstractRoom.getId(other.room))
                && Objects.equal(User.getId(this.sender), User.getId(other.sender))
                && Objects.equal(this.timeStamp, other.timeStamp);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("message", message)
                .add("roomId", AbstractRoom.getId(room))
                .add("userId", User.getId(sender))
                .add("timeStamp", timeStamp)
                .toString();
    }
}
