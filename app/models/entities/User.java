package models.entities;

import com.google.common.base.Objects;
import models.NoUpdate;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


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

    public static Finder<String, User> find = new Finder<>(String.class, User.class);

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("facebookId", facebookId)
                .add("name", name)
                .add("registrationId", registrationId)
                .toString();
    }
}
