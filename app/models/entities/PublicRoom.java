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
import java.util.*;


@Entity
@Table(name = "public_rooms")
public class PublicRoom extends AbstractRoom {

    public static final String ENTITY_NAME = "Public Room";

    @Constraints.Required
    @NoUpdate
    public String name;

    @Constraints.Required
    @Column(columnDefinition = "NUMERIC")
    @NoUpdate
    public Double latitude;

    @Constraints.Required
    @Column(columnDefinition = "NUMERIC")
    @NoUpdate
    public Double longitude;

    @Constraints.Required
    @NoUpdate
    public Integer radius;

    public Integer score = 0;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "subscriptions", joinColumns = {@JoinColumn(name = "roomId")}, inverseJoinColumns = {@JoinColumn(name = "userId")})
    public Set<User> subscribers = new LinkedHashSet<>();

    public static List<PublicRoom> allInGeoRange(double lat, double lon) {
        Logger.debug("Getting all rooms containing location " + lat + ", " + lon);

        int earthRadius = 6371; // in km

        String firstCutSql = "select r2.roomId" +
                " from PublicRoom r2" +
                " where :lat >= r2.latitude - degrees((r2.radius * 1000) / :R) and :lat <= r2.latitude + degrees((r2.radius * 1000) / :R)" +
                " and :lon >= r2.longitude - degrees((r2.radius * 1000) / :R) and :lon <= r2.longitude + degrees((r2.radius * 1000) / :R)";

        String sql = "select r" +
                " from PublicRoom r" +
                " where r.roomId in (" + firstCutSql + ") and" +
                " acos(sin(radians(:lat)) * sin(radians(latitude)) + cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lon))) * :R * 1000 <= radius";

        TypedQuery<PublicRoom> query = JPA.em().createQuery(sql, PublicRoom.class)
                .setParameter("lat", lat)
                .setParameter("lon", lon)
                .setParameter("R", earthRadius);

        return query.getResultList();
    }

    public void addSubscription(User user) {
        subscribers.add(user);
    }

    public void removeSubscription(long userId) {
        Optional<User> userOptional = subscribers.stream().filter(user -> user.userId == userId).findFirst();
        if (userOptional.isPresent()) {
            subscribers.remove(userOptional.get());
        } else {
            Logger.warn("Could not find a subscription with userId " + userId + " in " + this.name);
        }
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

            if (!androidRegIds.isEmpty()) {
                NotificationUtils.sendBatchAndroidNotifications(androidRegIds, data);
            }

            if (!iosRegIds.isEmpty()) {
                NotificationUtils.sendBatchAppleNotifications(iosRegIds, data);
            }
        }).start();
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
        final PublicRoom other = (PublicRoom) obj;

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
