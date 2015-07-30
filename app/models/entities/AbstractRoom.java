package models.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "abstract_rooms")
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public long roomId;

    @JsonIgnore
    public long createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    public long lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

    @JsonIgnore
    @OneToMany(targetEntity = Message.class, mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Message> messages = new ArrayList<>();

    // http://www.artima.com/lejava/articles/equality.html
    public boolean canEqual(Object other) {
        return other instanceof AbstractRoom;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AbstractRoom)) return false;
        AbstractRoom that = (AbstractRoom) other;
        return that.canEqual(this) &&
                Objects.equal(createdAt, that.createdAt) &&
                Objects.equal(lastActivity, that.lastActivity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(createdAt, lastActivity);
    }

    public static long getId(AbstractRoom room) {
        return room == null ? -1 : room.roomId;
    }
}
