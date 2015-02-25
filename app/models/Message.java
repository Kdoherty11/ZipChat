package models;

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
    public Long id;

    @Constraints.Required
    public String message;

    @Constraints.Required
    public long roomId;

    @Constraints.Required
    public long userId;

    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date timeStamp = new Date();

    public static List<Message> getByRoomId(long roomId) {
        String queryString = "select m from Message m where m.roomId = :roomId";

        Query query = JPA.em().createQuery(queryString)
                .setParameter("roomId", roomId);

        return query.getResultList();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("message", message)
                .add("roomId", roomId)
                .add("userId", userId)
                .add("timeStamp", timeStamp)
                .toString();
    }
}
