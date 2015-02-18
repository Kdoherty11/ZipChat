package models.entities;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.google.common.base.Objects;
import play.Logger;
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

        Logger.debug("Getting all rooms containing " + lat + ", " + lon);

        int earthRadius = 6371;  // earth's mean radius, km

        String firstCutSql = "select r.id, r.latitude, r.longitude, r.radius" +
                " from rooms r" +
                " where :lat >= r.latitude - degrees((r.radius * 1000) / :R) and :lat <= r.latitude + degrees((r.radius * 1000) / :R)" +
                " and :lon >= r.longitude - degrees((r.radius * 1000) / :R) and :lon <= r.longitude + degrees((r.radius * 1000) / :R)";

        String sql = "select id" +
                " from (" + firstCutSql + ") as FirstCut" +
                " where acos(sin(radians(:lat)) * sin(radians(latitude)) + cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - :lon)) * :R <= radius / 1000;";

        RawSql rawSql = RawSqlBuilder.parse(sql).create();

        Query<Room> query = Ebean.find(Room.class)
                .setRawSql(rawSql)
                .setParameter("lat", lat)
                .setParameter("lon", lon)
                .setParameter("R", earthRadius);

        return query.findList();
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
