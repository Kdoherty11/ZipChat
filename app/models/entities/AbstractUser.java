package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import notifications.AbstractNotification;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "abstract_users")
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractUser {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public long userId;

    public String facebookId;

    @JsonIgnore
    public String gender;

    @Constraints.Required
    public String name;

    @JsonIgnore
    public long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public static long getId(AbstractUser user) {
        return user == null ? -1 : user.userId;
    }

    @JsonIgnore
    public abstract boolean isAnon();
    @JsonIgnore
    public abstract User getActual();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractUser that = (AbstractUser) o;
        return Objects.equal(createdAt, that.createdAt) &&
                Objects.equal(facebookId, that.facebookId) &&
                Objects.equal(gender, that.gender) &&
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(facebookId, gender, name, createdAt);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userId", userId)
                .add("facebookId", facebookId)
                .add("gender", gender)
                .add("name", name)
                .add("createdAt", createdAt)
                .toString();
    }
}
