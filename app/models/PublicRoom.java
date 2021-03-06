package models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "public_rooms")
public class PublicRoom extends AbstractRoom {

    @Column(length = 50)
    @Constraints.Required
    public String name;

    @Constraints.Required
    @Constraints.Min(-90)
    @Constraints.Max(90)
    @Column(columnDefinition = "NUMERIC")
    public Double latitude;

    @Constraints.Required
    @Constraints.Min(-180)
    @Constraints.Max(180)
    @Column(columnDefinition = "NUMERIC")
    public Double longitude;

    @Constraints.Required
    public Integer radius; // in meters

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "creator")
    public User creator;

    @JsonIgnore
    @OneToMany(targetEntity = AnonUser.class, mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<AnonUser> anonUsers = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "subscriptions", joinColumns = {@JoinColumn(name = "roomId")}, inverseJoinColumns = {@JoinColumn(name = "userId")})
    public Set<User> subscribers = new LinkedHashSet<>();

    @Transient
    @Override
    public AbstractRoom.RoomType getType() {
        return AbstractRoom.RoomType.PUBLIC;
    }

    @Override
    public boolean canEqual(Object other) {
        return other instanceof PublicRoom;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof PublicRoom)) return false;
        PublicRoom that = (PublicRoom) other;
        return that.canEqual(this) && super.equals(that) &&
                Objects.equal(name, that.name) &&
                Objects.equal(latitude, that.latitude) &&
                Objects.equal(longitude, that.longitude) &&
                Objects.equal(radius, that.radius) &&
                Objects.equal(creator, that.creator);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), name, latitude, longitude, radius, creator);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("roomId", roomId)
                .add("name", name)
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("radius", radius)
                .add("creator", creator)
                .add("anonUsers", anonUsers)
                .add("subscribers", subscribers)
                .add("createdAt", createdAt)
                .add("lastActivity", lastActivity)
                .toString();
    }
}
