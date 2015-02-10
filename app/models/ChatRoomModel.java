package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by kevindoherty on 2/9/15.
 */
@Entity
public class ChatRoomModel extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String name;

    public ChatRoomModel(String name) {
        this.name = name;
    }
}
