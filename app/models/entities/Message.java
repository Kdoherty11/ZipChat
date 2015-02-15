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

    @ManyToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    public Room room;

    @ManyToOne(cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    @Constraints.Required
    public User user;

    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date timeStamp = new Date();

    public static Finder<String, Message> find = new Finder<>(String.class, Message.class);

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("message", message)
                .add("room", user.id)
                .add("user", room.id)
                .add("timeStamp", timeStamp)
                .toString();
    }


}
