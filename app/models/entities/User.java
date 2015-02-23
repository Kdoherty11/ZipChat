package models.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import controllers.NotificationUtils;
import models.Platform;
import play.Logger;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.libs.F;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Map;
import java.util.Optional;

import static play.libs.Json.toJson;


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

    public F.Promise<JsonNode> sendNotification(Map<String, String> data) {
        switch (platform) {
            case android:
                return NotificationUtils.sendAndroidNotification(registrationId, Optional.ofNullable(data));
            case ios:
                Logger.debug("send ios notification");
                NotificationUtils.sendAppleNotification();
                return F.Promise.promise(new F.Function0<JsonNode>() {
                    @Override
                    public JsonNode apply() throws Throwable {
                        return toJson("OK");
                    }
                });
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
