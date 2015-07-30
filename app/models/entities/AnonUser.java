package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by kevin on 6/10/15.
 */
@Entity
@Table(name = "anon_users")
public class AnonUser extends AbstractUser {

    @ManyToOne
    @JoinColumn(name = "actualUserId")
    @JsonIgnore
    @Constraints.Required
    public User actual;

    @ManyToOne
    @JoinColumn(name = "roomId")
    @JsonIgnore
    @Constraints.Required
    public PublicRoom room;

    public AnonUser() {
        // Needed for JPA
    }

    public AnonUser(User actual, PublicRoom room, String name) {
        this.actual = actual;
        this.room = room;
        this.name = name;
    }

    @Override
    public User getActual() {
        return actual;
    }

    @Override
    public boolean canEqual(Object other) {
        return other instanceof AnonUser;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AnonUser)) return false;
        AnonUser that = (AnonUser) other;
        return that.canEqual(this) && super.equals(other) &&
                Objects.equal(actual, that.actual) &&
                Objects.equal(room, that.room);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), actual, room);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", super.name)
                .add("actualId", User.getId(actual))
                .add("roomId", PublicRoom.getId(room))
                .toString();
    }
}
