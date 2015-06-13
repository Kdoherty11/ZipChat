package models.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import notifications.AbstractNotification;
import notifications.MessageNotification;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "abstract_rooms")
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public long roomId;

    public long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public long lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    @JsonIgnore
    @OneToMany(targetEntity = Message.class, mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Message> messages = new ArrayList<>();

    public void addMessage(Message message, Set<Long> userIdsInRoom) {
        messages.add(message);
        lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        sendNotification(new MessageNotification(message), userIdsInRoom);
    }

    abstract void sendNotification(AbstractNotification notification, Set<Long> userIdsInRoom);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoom that = (AbstractRoom) o;
        return Objects.equal(createdAt, that.createdAt) &&
                Objects.equal(lastActivity, that.lastActivity) &&
                Objects.equal(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(createdAt, lastActivity, messages);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("roomId", roomId)
                .add("createdAt", createdAt)
                .add("lastActivity", lastActivity)
                .add("messages", messages)
                .toString();
    }
}
