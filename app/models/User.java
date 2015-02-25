package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import play.Logger;
import play.data.validation.Constraints;
import play.libs.F;
import utils.DbUtils;
import utils.NotificationUtils;

import javax.persistence.*;
import java.util.Map;
import java.util.Optional;

import static play.libs.Json.toJson;


@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long userId;

    @Constraints.Required
    public String facebookId;

    @Constraints.Required
    public String name;

    public String registrationId;

    @Constraints.Required
    public Platform platform;

//    @ManyToMany
//    @JoinTable(name = "subscriptions", joinColumns = {@JoinColumn(name="userId")}, inverseJoinColumns = {@JoinColumn(name="roomId")})
//    public List<Room> subscribedTo;

    public static F.Promise<JsonNode> sendNotification(long id, Map<String, String> data) {
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