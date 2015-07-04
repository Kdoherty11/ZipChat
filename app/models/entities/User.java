package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import models.Platform;
import notifications.AbstractNotification;
import notifications.ChatRequestNotification;
import play.db.jpa.JPA;
import play.libs.ws.WS;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Entity
@Table(name = "users")
public class User extends AbstractUser {

    public User() { }

    @JsonIgnore
    @OneToMany(targetEntity = Device.class, mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Device> devices = new ArrayList<>();

    @Override
    public void sendNotification(AbstractNotification notification) {
        if (devices.isEmpty()) {
            return;
        }

        List<String> androidRegIds = new ArrayList<>();
        List<String> iosRegIds = new ArrayList<>();

        for (Device info : devices) {
            if (info.platform == Platform.android) {
                androidRegIds.add(info.regId);
            } else {
                iosRegIds.add(info.regId);
            }
        }

        notification.send(androidRegIds, iosRegIds);
    }

    @Override
    public boolean isAnon() {
        return false;
    }

    @Override
    public User getActual() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equal(devices, user.devices);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), devices);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("userId", userId)
                .add("facebookId", facebookId)
                .add("gender", gender)
                .add("name", name)
                .add("devices", devices)
                .add("createdAt", createdAt)
                .toString();
    }

}
