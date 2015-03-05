package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import models.NoUpdate;
import models.Platform;
import play.Logger;
import play.data.validation.Constraints;
import utils.DbUtils;
import utils.NotificationUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;


@Entity
@Table(name = "users")
public class User {

    public static final String ENTITY_NAME = "User";

    @Id
    @SequenceGenerator(name="user_userId_seq",
            sequenceName="user_userId_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.IDENTITY,
            generator="user_userId_seq")
    public long userId;

    @Constraints.Required
    public String facebookId;

    @Constraints.Required
    public String name;

    @JsonIgnore
    public String registrationId;

    @Constraints.Required
    @JsonIgnore
    public Platform platform;

    @NoUpdate
    @JsonIgnore
    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public static long getId(User user) {
        return user == null ? -1 : user.userId;
    }

    public static String sendNotification(long id, Map<String, String> data) {
        Optional<User> userOptional = DbUtils.findEntityById(User.class, id);

        if (userOptional.isPresent()) {
            return userOptional.get().sendNotification(data);
        } else {
            return DbUtils.buildEntityNotFoundString("User", id);
        }
    }

    public String sendNotification(Map<String, String> data) {
        if (Strings.isNullOrEmpty(registrationId)) {
            String error = "No registrationId found for user: " + this;
            Logger.error(error);
            return error;
        }
        switch (platform) {
            case android:
                return NotificationUtils.sendAndroidNotification(registrationId, data);
            case ios:
                return NotificationUtils.sendAppleNotification(registrationId, data);
            default:
                String error = "Attempted to send a notification to an " + platform + " device";
                Logger.error(error);
                return error;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, facebookId, name, registrationId, platform);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        return Objects.equal(this.userId, other.userId)
                && Objects.equal(this.facebookId, other.facebookId)
                && Objects.equal(this.name, other.name)
                && Objects.equal(this.registrationId, other.registrationId)
                && Objects.equal(this.platform, other.platform);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", userId)
                .add("name", name)
                .add("facebookId", facebookId)
                .add("registrationId", registrationId)
                .add("platform", platform)
                .toString();
    }
}
