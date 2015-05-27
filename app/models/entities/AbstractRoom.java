package models.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import models.NoUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "abstract_rooms")
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public class AbstractRoom {

    public static final String ENTITY_NAME = "Room";

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

    public void addMessage(Message message) {
        messages.add(message);
        lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
    }
}
