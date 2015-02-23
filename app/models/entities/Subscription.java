package models.entities;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Id;

/**
 * Created by kevindoherty on 2/19/15.
 */
public class Subscription extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String roomId;

    @Constraints.Required
    public String userId;

    public static Model.Finder<String, Subscription> find = new Model.Finder<>(String.class, Subscription.class);
}
