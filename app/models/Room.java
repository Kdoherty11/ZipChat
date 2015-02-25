package models;

import com.google.common.base.Objects;
import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long roomId;

    @Constraints.Required
    @NoUpdate
    public String name;

    @Constraints.Required
    @Column(columnDefinition = "NUMERIC")
    @NoUpdate
    public double latitude;

    @Constraints.Required
    @Column(columnDefinition = "NUMERIC")
    @NoUpdate
    public double longitude;

    @Constraints.Required
    @NoUpdate
    public int radius;

    @Formats.DateTime(pattern="dd/MM/yyyy")
    @NoUpdate
    public Date creationTime = new Date();

    @Formats.DateTime(pattern="dd/MM/yyyy")
    @NoUpdate
    public Date lastActivity = new Date();

//    @ManyToMany
//    @JoinTable(name = "subscriptions", joinColumns = {@JoinColumn(name="roomId")}, inverseJoinColumns = {@JoinColumn(name="userId")})
//    public List<Long> subscribers = new ArrayList<>();

    public int score;

    public static List<Room> allInGeoRange(double lat, double lon) {

        Logger.debug("Getting all rooms containing " + lat + ", " + lon);

        int earthRadius = 6371;  // earth's mean radius, km

        String firstCutSql = "select r" +
                " from Room r" +
                " where :lat >= r.latitude - degrees((r.radius * 1000) / :R) and :lat <= r.latitude + degrees((r.radius * 1000) / :R)" +
                " and :lon >= r.longitude - degrees((r.radius * 1000) / :R) and :lon <= r.longitude + degrees((r.radius * 1000) / :R)";

        // TODO: Fix for JPA
        String sql = "select r" +
                " from (" + firstCutSql + ") as FirstCut" +
                " where acos(sin(radians(:lat)) * sin(radians(latitude)) + cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lon))) * :R * 1000 <= radius;";

        Query query = JPA.em().createQuery(firstCutSql, Room.class)
                .setParameter("lat", lat)
                .setParameter("lon", lon)
                .setParameter("R", earthRadius);

        return query.getResultList();
    }

//    public void notifySubscribers(Map<String, String> data) {
//        subscribers.forEach(user -> user.sendNotification(data));
//    }

    //public void addSubscription(long userId) {
   //     subscribers.add(userId);
   // }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("roomId", roomId)
                .add("name", name)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("radius", radius)
                .add("score", score)
                .add("creationTime", creationTime)
                .add("lastActivity", lastActivity)
                .toString();
    }

}
