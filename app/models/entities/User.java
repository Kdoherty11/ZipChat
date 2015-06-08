package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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
    @OneToMany(targetEntity = NotificationInfo.class, mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<NotificationInfo> notificationInfoList;

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
        return WS.url("https://graph.facebook.com/me").setQueryParameter("access_token", fbAccessToken).get().get(5, TimeUnit.SECONDS).asJson();
    }

    public static long getId(User user) {
        return user == null ? -1 : user.userId;
    }

    public void addNotificationInfo(NotificationInfo notificationInfo) {
        notificationInfoList.add(notificationInfo);
    }

    public static void sendNotification(long userId, Map<String, String> data) {
        Optional<User> userOptional = DbUtils.findEntityById(User.class,userId);
        if (userOptional.isPresent()) {
            userOptional.get().sendNotification(data);
        } else {
            Logger.error(DbUtils.buildEntityNotFoundString(User.ENTITY_NAME, userId));
        }
    }

    public void sendNotification(Map<String, String> data) {
        if (notificationInfoList.isEmpty()) {
            return;
        }

        List<String> androidRegIds = new ArrayList<>();
        List<String> iosRegIds = new ArrayList<>();

        for (NotificationInfo notificationInfo : notificationInfoList) {
            if (notificationInfo.platform == Platform.android) {
                androidRegIds.add(notificationInfo.registrationId);
            } else {
                iosRegIds.add(notificationInfo.registrationId);
            }
        }

        if (androidRegIds.size() == 1) {
            NotificationUtils.sendAndroidNotification(androidRegIds.get(0), data);
        } else {
            NotificationUtils.sendBatchAndroidNotifications(androidRegIds, data);
        }

        if (iosRegIds.size() == 1) {
            NotificationUtils.sendAppleNotification(iosRegIds.get(0), data);
        } else {
            NotificationUtils.sendBatchAppleNotifications(iosRegIds, data);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, facebookId, name, notificationInfoList);
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
                && Objects.equal(this.notificationInfoList, other.notificationInfoList);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userId", userId)
                .add("name", name)
                .add("facebookId", facebookId)
                .add("notificationInfoList", notificationInfoList)
                .toString();
    }
}
