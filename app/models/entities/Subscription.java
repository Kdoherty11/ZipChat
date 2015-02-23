package models.entities;

import com.avaje.ebean.FutureList;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "subscriptions")
public class Subscription extends Model {

    @Id
    public String id;

    @Constraints.Required
    public String roomId;

    @Constraints.Required
    public String userId;

    public static Model.Finder<String, Subscription> find = new Model.Finder<>(String.class, Subscription.class);

    public static FutureList<Subscription> findByRoomId(String roomId) {
        return find.select("userId").where().eq("roomId", roomId).findFutureList();
    }
}
