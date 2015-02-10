package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Room extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String name;

    public Room(String name) {
        this.name = name;
    }


}