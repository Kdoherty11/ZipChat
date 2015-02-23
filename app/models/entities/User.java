package models.entities;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import controllers.NotificationUtils;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.libs.F;
import play.libs.ws.WSResponse;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Map;
import java.util.Optional;


@Entity
@Table(name = "users")
public class User extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String facebookId;

    @Constraints.Required
    public String name;

    public String registrationId;

    public static Finder<String, User> find = new Finder<>(String.class, User.class);

    public F.Promise<WSResponse> sendNotification(Map<String, String> data) {
        if (!Strings.isNullOrEmpty(registrationId)) {
            return NotificationUtils.sendAndroidNotification(new String[] {registrationId}, Optional.ofNullable(data));
        }

        NotificationUtils.sendAppleNotification();
        return null;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("facebookId", facebookId)
                .add("name", name)
                .add("registrationId", registrationId)
                .toString();
    }
}
