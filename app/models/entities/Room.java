package models.entities;

import com.google.common.base.Objects;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "rooms")
public class Room extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public double latitude;

    @Constraints.Required
    public double longitude;

    @Constraints.Required
    public int radius;

    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date creationTime;

    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date lastActivity;

    public int score;

    public static Finder<String, Room> find = new Finder<>(String.class, Room.class);

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("radius", radius)
                .add("creationTime", creationTime)
                .add("lastActivity", lastActivity)
                .add("score", score)
                .toString();
    }




}
