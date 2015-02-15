package models.entities;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "rooms")
public class Room extends Model {

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

    public int score;

    public Room(String name, int radius, double latitude, double longitude) {
        this.name = name;
        this.radius = radius;
        this.latitude = latitude;
        this.longitude = longitude;
        this.score = 0;
    }

}
