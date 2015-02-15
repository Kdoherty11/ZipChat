package models.entities;

import models.NoUpdate;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by zachwebert on 2/9/15.
 */

@Entity
@Table(name = "users")
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
