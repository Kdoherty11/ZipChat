package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import models.Platform;
import notifications.AbstractNotification;
import notifications.ChatRequestNotification;
import play.Logger;
import play.db.jpa.JPA;
import play.libs.ws.WS;
import utils.DbUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Entity
@Table(name = "users")
public class User extends AbstractUser {

    @JsonIgnore
    @OneToMany(targetEntity = Device.class, mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Device> devices;

    public List<Device> getDevices() {
        return devices;
    }

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

    public void sendChatRequest(AbstractUser receiver) {
        User actualReceiver = receiver.getActual();
        JPA.em().persist(new Request(this, actualReceiver));
        actualReceiver.sendNotification(new ChatRequestNotification(this));
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
}
