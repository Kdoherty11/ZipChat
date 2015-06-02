package models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import models.ForeignEntity;
import org.hibernate.annotations.GenericGenerator;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import utils.DbUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "messages")
public class Message {

    public static final String ENTITY_NAME = "Message";

    @Id
    @GenericGenerator(name = "messages_gen", strategy = "sequence", parameters = {
            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "messages_gen"),
            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
    })
    @GeneratedValue(generator = "messages_gen", strategy = GenerationType.SEQUENCE)
    public long messageId;

    @Constraints.Required
    public String message;

    @ManyToOne
    @JoinColumn(name = "roomId")
    @JsonIgnore
    @ForeignEntity
    public AbstractRoom room;

    @Constraints.Required
    public long senderId;

    @Constraints.Required
    public String senderName;

    public String senderFbId;

    public boolean isAnon;

    @ManyToMany(targetEntity = User.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "message_favorites", joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "messageId"))
    public List<User> favorites = new ArrayList<>();

    @ManyToMany(targetEntity = User.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "message_flags", joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "messageId"))
    public List<User> flags = new ArrayList<>();

    public int score;

    public long timeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public Message() {
    }

    public Message(long roomId, long senderId, String senderName, String senderFbId, String message, boolean isAnon) {
        Optional<AbstractRoom> roomOptional = DbUtils.findEntityById(AbstractRoom.class, roomId);
        if (roomOptional.isPresent()) {
            this.room = roomOptional.get();
        } else {
            throw new IllegalArgumentException(DbUtils.buildEntityNotFoundString(AbstractRoom.ENTITY_NAME, roomId));
        }
        this.message = Preconditions.checkNotNull(message);
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderFbId = senderFbId;
        this.isAnon = isAnon;
    }

    public void addToRoom() {
        if (room != null) {
            room.addMessage(this);
        } else {
            Logger.error(this + " has a null room");
        }
    }

    public boolean favorite(User user) {
        if (favorites.contains(user)) {
            Logger.error(user + " is attempting to favorite " + this + " but has already favorited it");
            return false;
        }
        favorites.add(user);
        score++;
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

    public static List<Message> getMessages(long roomId, int limit, int offset) {

        String queryString = "select m from Message m where m.room.roomId = :roomId order by m.timeStamp DESC";

        TypedQuery<Message> limitOffsetQuery = JPA.em().createQuery(queryString, Message.class)
                .setParameter("roomId", roomId)
                .setMaxResults(limit)
                .setFirstResult(offset);

        List<Message> messages = limitOffsetQuery.getResultList();

        Collections.reverse(messages);

        return messages;
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
        return Objects.equal(messageId, message1.messageId) &&
                Objects.equal(senderId, message1.senderId) &&
                Objects.equal(isAnon, message1.isAnon) &&
                Objects.equal(score, message1.score) &&
                Objects.equal(timeStamp, message1.timeStamp) &&
                Objects.equal(message, message1.message) &&
                Objects.equal(room, message1.room) &&
                Objects.equal(senderName, message1.senderName) &&
                Objects.equal(senderFbId, message1.senderFbId) &&
                Objects.equal(flags, message1.flags) &&
                Objects.equal(favorites, message1.favorites);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageId, message, room, senderId, senderName, senderFbId, isAnon, favorites, flags, score, timeStamp);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("messageId", messageId)
                .add("message", message)
                .add("room", room)
                .add("senderId", senderId)
                .add("senderName", senderName)
                .add("senderFbId", senderFbId)
                .add("isAnon", isAnon)
                .add("flags", flags)
                .add("favorites", favorites)
                .add("score", score)
                .add("timeStamp", timeStamp)
                .toString();
    }
}
