package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import play.data.validation.Constraints;

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