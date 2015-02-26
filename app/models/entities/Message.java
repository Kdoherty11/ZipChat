package models.entities;

import com.google.common.base.Objects;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

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
    public long userId;

    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date timeStamp = new Date();

    public static List<Message> getByRoomId(String roomId) {
        String queryString = "select m from Message m where m.roomId = :roomId";

        TypedQuery<Message> query = JPA.em().createQuery(queryString, Message.class)
                .setParameter("roomId", roomId);

        return query.getResultList();
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
                && Objects.equal(this.room, other.room)
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
