package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import notifications.MessageFavoritedNotification;
import org.hibernate.annotations.GenericGenerator;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public List<User> favorites = new ArrayList<>();

    @ManyToMany(targetEntity = User.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "message_flags", joinColumns = @JoinColumn(name = "messageId"),
            inverseJoinColumns = @JoinColumn(name = "userId"))
    public List<User> flags = new ArrayList<>();

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

    public boolean favorite(User user) {
        if (favorites.contains(user)) {
            Logger.error(user + " is attempting to favorite " + this + " but has already favorited it");
            return false;
        }
        favorites.add(user);
        score++;
        User actual = sender.getActual();
        if (!user.equals(actual)) {
            actual.sendNotification(new MessageFavoritedNotification(this, user));
        }
        return true;
    }

    public boolean removeFavorite(User user) {
        boolean didDeleteUser = favorites.remove(user);
        if (didDeleteUser) {
            score--;
        } else {
            Logger.warn(user + " attempted to remove favorite from " + this + " but has not favorited it");
        }
        return didDeleteUser;
    }

    public boolean flag(User user) {
        if (flags.contains(user)) {
            Logger.error(user + " is attempting to flag " + this + " but has already flagged it");
            return false;
        }

        flags.add(user);
        return true;
    }

    public boolean removeFlag(User user) {
        boolean didDeleteUser = flags.remove(user);
        if (!didDeleteUser) {
            Logger.warn(user + " attempted to remove a flag from " + this + " but has not flagged it");
        }
        return didDeleteUser;
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
        return Objects.toStringHelper(this)
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
