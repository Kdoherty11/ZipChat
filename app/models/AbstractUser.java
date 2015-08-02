package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
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
    public abstract User getActual();

    // http://www.artima.com/lejava/articles/equality.html
    public boolean canEqual(Object other) {
        return other instanceof AbstractUser;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AbstractUser)) return false;
        AbstractUser that = (AbstractUser) other;
        return that.canEqual(this) &&
                Objects.equal(createdAt, that.createdAt) &&
                Objects.equal(facebookId, that.facebookId) &&
                Objects.equal(gender, that.gender) &&
                Objects.equal(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(facebookId, gender, name, createdAt);
    }
}
