package models.entities;

import com.google.common.base.Objects;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "messages")
public class Message extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String message;

    @Constraints.Required
    public String roomId;

    @Constraints.Required
    public String userId;

    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date timeStamp = new Date();

    public static Finder<String, Message> find = new Finder<>(String.class, Message.class);

    public Message(String message, String roomId, String userId) {
        this.message = message;
        this.roomId = roomId;
        this.userId = userId;
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
