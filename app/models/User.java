package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by zachwebert on 2/9/15.
 */

@Entity
public class User extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String facebookId;

    @Constraints.Required
    public String name;

    @NoUpdate
    public String registrationId;

}
