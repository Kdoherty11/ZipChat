package models.entities;

import com.avaje.ebean.Ebean;
import com.google.common.base.Objects;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

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
    public Date creationTime = new Date();

    @Formats.DateTime(pattern="dd/MM/yyyy")
    public Date lastActivity = new Date();

    private int distance;

    public int score;

    public static Finder<String, Room> find = new Finder<>(String.class, Room.class);

    public static List<Room> allInGeoRange(double lat, double lon) {

        int earthRadius = 6371;  // earth's mean radius, km

        String sql = "select id, name, latitude, longitude, radius, lastActivity, score," +
                " acos(sin(:lat)*sin(radians(latitude)) + cos(:lat)*cos(radians(latitude))*cos(radians(longitude)-:lon)) * :R As distance" +
                " from rooms" +
                " where acos(sin(:lat)*sin(radians(latitude)) + cos(:lat)*cos(radians(latitude))*cos(radians(longitude)-:lon)) * :R <= radius" +
                " order by distance";

        return Ebean.createQuery(Room.class, sql)
                .setParameter("lat", lat)
                .setParameter("lon", lon)
                .setParameter("R", earthRadius)
                .findList();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("radius", radius)
                .add("distance", distance)
                .add("score", score)
                .add("creationTime", creationTime)
                .add("lastActivity", lastActivity)
                .toString();
    }

}
