package models.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import utils.NotificationUtils;
import models.Platform;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.libs.F;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Map;


@Entity
@Table(name = "users")
public class User extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String facebookId;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public String registrationId;

    @Constraints.Required
    public Platform platform;

    public static Finder<String, User> find = new Finder<>(String.class, User.class);

    public static F.Promise<JsonNode> sendNotification(String id, Map<String, String> data) {
        F.Promise<User> userPromise = F.Promise.promise(new F.Function0<User>() {
            @Override
            public User apply() throws Throwable {
                return find.byId(id);
            }
        });
        return userPromise.flatMap(new F.Function<User, F.Promise<JsonNode>>() {
            @Override
            public F.Promise<JsonNode> apply(User user) throws Throwable {
                return user.sendNotification(data);
            }
        });
    }

    public F.Promise<JsonNode> sendNotification(Map<String, String> data) {
        switch (platform) {
            case android:
                return NotificationUtils.sendAndroidNotification(registrationId, data);
            case ios:
                NotificationUtils.sendAppleNotification();
                return null;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("facebookId", facebookId)
                .add("name", name)
                .add("registrationId", registrationId)
                .add("platform", platform)
                .toString();
    }
}
