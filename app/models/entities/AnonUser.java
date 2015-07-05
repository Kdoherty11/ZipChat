package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import notifications.AbstractNotification;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AnonUser anonUser = (AnonUser) o;
        return Objects.equal(actual, anonUser.actual) &&
                Objects.equal(room, anonUser.room);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), actual, room);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", super.name)
                .add("actualId", User.getId(actual))
                .add("roomId", PublicRoom.getId(room))
                .toString();
    }
}
