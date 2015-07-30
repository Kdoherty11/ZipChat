package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import models.Platform;
import org.hibernate.annotations.GenericGenerator;
import play.Logger;
import play.data.validation.Constraints;

import javax.persistence.*;

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
    @Constraints.Required
    public User user;

    public Device() {
        // Needed for JPA
    }

    public Device(User user, String regId, Platform platform) {
        this.user = Preconditions.checkNotNull(user);
        this.regId = Preconditions.checkNotNull(regId);
        this.platform = Preconditions.checkNotNull(platform);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Device)) return false;
        Device device = (Device) other;
        return Objects.equal(regId, device.regId) &&
                Objects.equal(platform, device.platform) &&
                Objects.equal(user, device.user);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(regId, platform, user);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("deviceId", deviceId)
                .add("regId", regId)
                .add("platform", platform)
                .add("userId", AbstractUser.getId(user))
                .toString();
    }
}
