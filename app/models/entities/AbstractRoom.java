package models.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import models.NoUpdate;
import models.Platform;
import utils.NotificationUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public class AbstractRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public long roomId;

    @NoUpdate
    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    @NoUpdate
    public long lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "subscriptions", joinColumns = {@JoinColumn(name = "roomId")}, inverseJoinColumns = {@JoinColumn(name = "userId")})
    public List<User> subscribers = new ArrayList<>();

    @JsonIgnore
    @OneToMany(targetEntity = Message.class, mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Message> messages = new ArrayList<>();

    public static long getId(Room room) {
        return room == null ? -1 : room.roomId;
    }

    public void addSubscription(User user) {
        subscribers.add(user);
    }

    public void addMessage(Message message) {
        messages.add(message);
        lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    }

    public void notifySubscribers(Map<String, String> data) {
        new Thread(() -> {
            List<String> androidRegIds = new ArrayList<>();
            List<String> iosRegIds = new ArrayList<>();

            subscribers.forEach(user -> {
                if (user.platform == Platform.android) {
                    androidRegIds.add(user.registrationId);
                } else if (user.platform == Platform.ios) {
                    iosRegIds.add(user.registrationId);
                }
            });

            NotificationUtils.sendBatchAndroidNotifications(androidRegIds, data);
            NotificationUtils.sendBatchAppleNotifications(iosRegIds, data);
        }).start();
    }
}
