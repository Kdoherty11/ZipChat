package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import models.Platform;
import org.hibernate.annotations.GenericGenerator;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

/**
 * Created by kevin on 6/7/15.
 */
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GenericGenerator(name = "devices_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "devices_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "devices_gen", strategy = GenerationType.SEQUENCE)
    public long deviceId;

    @Constraints.Required
    public String regId;

    @Constraints.Required
    public Platform platform;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonIgnore
    public User user;

    public Device() { }

    public Device(User user, String regId, Platform platform) {
        this.user = Preconditions.checkNotNull(user);
        this.regId = Preconditions.checkNotNull(regId);
        this.platform = Preconditions.checkNotNull(platform);
    }

    public static Optional<Device> getDevice(long userId, String regId, Platform platform) {
        String queryString = "select d from Device d where d.user.userId = :userId and d.regId = :regId and d.platform = :platform";

        TypedQuery<Device> query = JPA.em().createQuery(queryString, Device.class)
                .setParameter("userId", userId)
                .setParameter("regId", regId)
                .setParameter("platform", platform);

        List<Device> deviceList = query.getResultList();
        if (deviceList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(deviceList.get(0));
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("deviceId", deviceId)
                .add("regId", regId)
                .add("platform", platform)
                .add("user", User.getId(user))
                .toString();
    }
}
