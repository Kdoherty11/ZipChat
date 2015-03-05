package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import models.NoUpdate;
import models.Platform;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import utils.NotificationUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "rooms")
public class Room extends AbstractRoom {

    public static final String ENTITY_NAME = "Room";

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

    @NoUpdate
    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    @NoUpdate
    public long lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "subscriptions", joinColumns = {@JoinColumn(name = "roomId")}, inverseJoinColumns = {@JoinColumn(name = "id")})
    public List<User> subscribers = new ArrayList<>();

    @JsonIgnore
    @OneToMany(targetEntity = Message.class, mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Message> messages = new ArrayList<>();

    public int score;

    public static long getId(Room room) {
        return room == null ? -1 : room.roomId;
    }

    public static List<Room> allInGeoRange(double lat, double lon) {
        Logger.debug("Getting all rooms containing location " + lat + ", " + lon);

        int earthRadius = 6371; // in km

        String firstCutSql = "select r2.id" +
                " from Room r2" +
                " where :lat >= r2.latitude - degrees((r2.radius * 1000) / :R) and :lat <= r2.latitude + degrees((r2.radius * 1000) / :R)" +
                " and :lon >= r2.longitude - degrees((r2.radius * 1000) / :R) and :lon <= r2.longitude + degrees((r2.radius * 1000) / :R)";

        String sql = "select r" +
                " from Room r" +
                " where r.id in (" + firstCutSql + ") and" +
                " acos(sin(radians(:lat)) * sin(radians(latitude)) + cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lon))) * :R * 1000 <= radius";

        TypedQuery<Room> query = JPA.em().createQuery(sql, Room.class)
                .setParameter("lat", lat)
                .setParameter("lon", lon)
                .setParameter("R", earthRadius);

        return query.getResultList();
    }

    public void notifySubscribers(Map<String, String> data) {
        new Thread(() -> {
            List<String> androidRegIds = new ArrayList<>();
            List<String> iosRegIds = new ArrayList<>();

            subscribers.forEach(user -> {
                if (user.platform == Platform.android) {
                    androidRegIds.add(user.registrationId);
                } else if (user.platform == Platform.ios) {
                    iosRegIds.add(user.registrationId);
                }
            });

            NotificationUtils.sendBatchAndroidNotifications(androidRegIds, data);
            NotificationUtils.sendBatchAppleNotifications(iosRegIds, data);
        }).start();
    }

    public void addSubscription(User user) {
        subscribers.add(user);
    }

    public void addMessage(Message message) {
        messages.add(message);
        lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roomId, name, latitude, longitude, radius, timeStamp, lastActivity, subscribers, score);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Room other = (Room) obj;
        return Objects.equal(this.roomId, other.roomId)
                && Objects.equal(this.name, other.name)
                && Objects.equal(this.latitude, other.latitude)
                && Objects.equal(this.longitude, other.longitude)
                && Objects.equal(this.radius, other.radius)
                && Objects.equal(this.timeStamp, other.timeStamp)
                && Objects.equal(this.lastActivity, other.lastActivity)
                && Objects.equal(this.subscribers, other.subscribers)
                && Objects.equal(this.score, other.score);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("roomId", roomId)
                .add("name", name)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("radius", radius)
                .add("score", score)
                .add("creationTime", timeStamp)
                .add("lastActivity", lastActivity)
                .toString();
    }

}
