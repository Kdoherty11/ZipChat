package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import models.Platform;
import org.hibernate.annotations.GenericGenerator;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import utils.DbUtils;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

/**
 * Created by kevin on 6/7/15.
 */
@Entity
@Table(name = "notification_info")
public class NotificationInfo {

    @Id
    @GenericGenerator(name = "notification_info_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "notification_info_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "notification_info_gen", strategy = GenerationType.SEQUENCE)
    public long notificationInfoId;

    @Constraints.Required
    public String regId;

    @Constraints.Required
    public Platform platform;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonIgnore
    public User user;

    public NotificationInfo(User user, String regId, Platform platform) {
        this.user = Preconditions.checkNotNull(user);
        this.regId = Preconditions.checkNotNull(regId);
        this.platform = Preconditions.checkNotNull(platform);
    }

    public void addToUser() {
        if (user != null) {
            user.addNotificationInfo(this);
        } else {
            Logger.error(this + " has a null user");
        }
    }

    public static Optional<NotificationInfo> getNotificationInfo(long userId, String regId, Platform platform) {
        String queryString = "select n from NotificationInfo n where n.user.userId = :userId and n.regId = :regId and n.platform = :platform";

        TypedQuery<NotificationInfo> query = JPA.em().createQuery(queryString, NotificationInfo.class)
                .setParameter("userId", userId)
                .setParameter("regId", regId)
                .setParameter("platform", platform);

        List<NotificationInfo> notificationInfoList = query.getResultList();
        if (notificationInfoList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(notificationInfoList.get(0));
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("notificationInfoId", notificationInfoId)
                .add("regId", regId)
                .add("platform", platform)
                .add("user", User.getId(user))
                .toString();
    }
}
