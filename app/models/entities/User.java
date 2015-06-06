package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import models.NoUpdate;
import models.Platform;
import org.hibernate.annotations.GenericGenerator;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import play.libs.ws.WS;
import utils.DbUtils;
import utils.NotificationUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Entity
@Table(name = "users")
@SequenceGenerator(name="users_userId_seq", sequenceName="users_userId_seq", allocationSize=10)
public class User {

    public static final String ENTITY_NAME = "User";

    @Id
    @GenericGenerator(name = "users_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "users_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "users_gen", strategy=GenerationType.SEQUENCE)
    public long userId;

    @Column(unique = true)
    @Constraints.Required
    public String facebookId;

    @JsonIgnore
    public String gender;

    @Constraints.Required
    public String name;

    @JsonIgnore
    @ElementCollection(fetch = FetchType.LAZY)
    public List<String> registrationIds;

    @JsonIgnore
    @Constraints.Required
    public Platform platform;

    @JsonIgnore
    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public static Optional<User> byFacebookId(String facebookId) {
        String queryString = "select u from User u where u.facebookId = :facebookId";

        TypedQuery<User> query = JPA.em().createQuery(queryString, User.class)
                .setParameter("facebookId", facebookId);

        List<User> users = query.getResultList();
        if (users.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(users.get(0));
        }
    }

    public static JsonNode getFacebookInformation(String fbAccessToken) {
        return WS.url("https://graph.facebook.com/me").setQueryParameter("access_token", fbAccessToken).get().get(3, TimeUnit.SECONDS).asJson();
    }

    public static long getId(User user) {
        return user == null ? -1 : user.userId;
    }

    public static String sendNotification(long userId, Map<String, String> data) {
        Optional<User> userOptional = DbUtils.findEntityById(User.class,userId);
        if (userOptional.isPresent()) {
            return userOptional.get().sendNotification(data);
        } else {
            return DbUtils.buildEntityNotFoundString(User.ENTITY_NAME, userId);
        }
    }

    public String sendNotification(Map<String, String> data) {
        if (registrationIds.isEmpty()) {
            String error = "No registrationId found for " + this;
            Logger.error(error);
            return error;
        }
        
        switch (platform) {
            case android:
                return NotificationUtils.sendBatchAndroidNotifications(registrationIds, data);
            case ios:
                return NotificationUtils.sendBatchAppleNotifications(registrationIds, data);
            default:
                throw new UnsupportedOperationException("Sending notifications to " + platform +
                        "devices is not supported");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, facebookId, name, registrationIds, platform);
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
                && Objects.equal(this.registrationIds, other.registrationIds)
                && Objects.equal(this.platform, other.platform);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userId", userId)
                .add("name", name)
                .add("facebookId", facebookId)
                .add("registrationIds", registrationIds)
                .add("platform", platform)
                .toString();
    }
}
