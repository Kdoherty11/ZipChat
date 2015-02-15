package models.entities;

import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    public String id;

    @Constraints.Required
    public String message;

    public String roomId;

    public User sender;

    public long timeStamp;



}
