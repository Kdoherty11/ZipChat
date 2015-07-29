package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.hibernate.annotations.GenericGenerator;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GenericGenerator(name = "messages_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "messages_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "messages_gen", strategy = GenerationType.SEQUENCE)
    public long messageId;

    @ManyToOne
    @JoinColumn(name = "roomId")
    @JsonIgnore
    @Constraints.Required
    public AbstractRoom room;

    @ManyToOne
    @JoinColumn(name = "userId")
    @Constraints.Required
    public AbstractUser sender;

    @Constraints.Required
    public String message;

    @ManyToMany(targetEntity = User.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "message_favorites", joinColumns = @JoinColumn(name = "messageId"),
            inverseJoinColumns = @JoinColumn(name = "userId"))
    public Set<User> favorites = new HashSet<>();

    @ManyToMany(targetEntity = User.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "message_flags", joinColumns = @JoinColumn(name = "messageId"),
            inverseJoinColumns = @JoinColumn(name = "userId"))
    public Set<User> flags = new HashSet<>();

    public int score;

    public long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public Message() {
        // Needed for JPA
    }

    public Message(AbstractRoom room, AbstractUser sender, String message) {
        this.room = Preconditions.checkNotNull(room);
        this.sender = Preconditions.checkNotNull(sender);
        this.message = Preconditions.checkNotNull(message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equal(score, message1.score) &&
                Objects.equal(createdAt, message1.createdAt) &&
                Objects.equal(room, message1.room) &&
                Objects.equal(sender, message1.sender) &&
                Objects.equal(message, message1.message) &&
                Objects.equal(favorites, message1.favorites) &&
                Objects.equal(flags, message1.flags);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(room, sender, message, favorites, flags, score, createdAt);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("messageId", messageId)
                .add("roomId", AbstractRoom.getId(room))
                .add("senderId", AbstractUser.getId(sender))
                .add("message", message)
                .add("favorites", favorites)
                .add("flags", flags)
                .add("score", score)
                .add("createdAt", createdAt)
                .toString();
    }
}
