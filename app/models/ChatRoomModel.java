package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ChatRoomModel extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    public double latitude;

    @Constraints.Required
    public double longitude;

    @Constraints.Required
    public int radius;

    public long timeStamp;

    public long lastActivity;

    public ChatRoomModel(String name, int radius, double latitude, double longitude) {
        this.name = name;
        this.radius = radius;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
