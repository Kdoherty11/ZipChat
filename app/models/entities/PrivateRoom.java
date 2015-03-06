package models.entities;

import models.NoUpdate;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
public class PrivateRoom extends AbstractRoom {
    @ManyToOne
    @JoinColumn(name="sender")
    @Constraints.Required
    public User sender;

    @ManyToOne
    @JoinColumn(name="receiver")
    @Constraints.Required
    public User receiver;
}
