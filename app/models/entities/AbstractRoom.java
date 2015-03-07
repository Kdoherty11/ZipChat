package models.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import models.NoUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

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
    @OneToMany(targetEntity = Message.class, mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<Message> messages = new ArrayList<>();

    public static long getId(AbstractRoom room) {
        return room == null ? -1 : room.roomId;
    }

    public void addMessage(Message message) {
        messages.add(message);
        lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    }
}
