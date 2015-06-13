package models.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import models.Platform;
import notifications.AbstractNotification;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.*;


@Entity
@Table(name = "public_rooms")
public class PublicRoom extends AbstractRoom {

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Column(columnDefinition = "NUMERIC")
    public Double latitude;

    @Constraints.Required
    @Column(columnDefinition = "NUMERIC")
    public Double longitude;

    @Constraints.Required
    public Integer radius;

    @JsonIgnore
    @OneToMany(targetEntity = AnonUser.class, mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<AnonUser> anonUsers = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "subscriptions", joinColumns = {@JoinColumn(name = "roomId")}, inverseJoinColumns = {@JoinColumn(name = "userId")})
    public Set<User> subscribers = new LinkedHashSet<>();

    public List<AnonUser> getAnonUsers() {
        return anonUsers;
    }

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
            Logger.warn("Could not find a subscription with userId " + userId + " in " + roomId);
        }
    }

    public boolean isSubscribed(long userId) {
        return subscribers.stream().anyMatch(user -> user.userId == userId);
    }

    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

    @Override
    void sendNotification(AbstractNotification notification, Set<Long> userIdsInRoom) {
        if (!hasSubscribers()) {
            return;
        }
        List<String> androidRegIds = new ArrayList<>();
        List<String> iosRegIds = new ArrayList<>();

        subscribers.forEach(user -> {
            if (!userIdsInRoom.contains(user.userId)) {
                user.getDevices().forEach(info -> {
                    if (info.platform == Platform.android) {
                        androidRegIds.add(info.regId);
                    } else {
                        iosRegIds.add(info.regId);
                    }
                });
            }
        });

        notification.send(androidRegIds, iosRegIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PublicRoom that = (PublicRoom) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(latitude, that.latitude) &&
                Objects.equal(longitude, that.longitude) &&
                Objects.equal(radius, that.radius);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), name, latitude, longitude, radius);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("radius", radius)
                .add("anonUsers", anonUsers)
                .add("subscribers", subscribers)
                .toString();
    }
}
