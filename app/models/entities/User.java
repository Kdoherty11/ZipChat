package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import models.NoUpdate;
import models.Platform;
import play.Logger;
import play.data.validation.Constraints;
import play.libs.F;
import utils.DbUtils;
import utils.NotificationUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static play.libs.Json.toJson;


@Entity
@Table(name = "users")
public class User {

    public static final String ENTITY_NAME = "User";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public String userId;

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

    public static String getId(User user) {
        return user == null ? "" : user.userId;
    }

    public static F.Promise<JsonNode> sendNotification(String id, Map<String, String> data) {

        Optional<User> userOptional = DbUtils.findEntityById(User.class, id);

        if (userOptional.isPresent()) {
            return userOptional.get().sendNotification(data);
        } else {
            return F.Promise.promise(() -> toJson(DbUtils.buildEntityNotFoundError("User", id)));
        }
    }

    public F.Promise<JsonNode> sendNotification(Map<String, String> data) {
        if (Strings.isNullOrEmpty(registrationId)) {
            return F.Promise.promise(() -> toJson("No registration ID found for user " + this));
        }
        switch (platform) {
            case android:
                return NotificationUtils.sendAndroidNotification(registrationId, data);
            case ios:
                NotificationUtils.sendAppleNotification(registrationId, data);
                return F.Promise.promise(() -> toJson("OK"));
            default:
                Logger.error("Attempted to send a notification to an " + platform + " device");
                return F.Promise.promise(() -> toJson("Sending notifications to " + platform + " devices is not supported"));
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
