package models.entities;

import com.avaje.ebean.annotation.EnumValue;
import models.NoUpdate;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "requests")
public class Request extends Model {

    @Id
    public String id;

    @Constraints.Required
    @NoUpdate
    public String toUserId;

    @Constraints.Required
    @NoUpdate
    public String fromUserId;

    @Constraints.Required
    public Status status = Status.pending;

    @NoUpdate
    public String message;

    public static Model.Finder<String, Request> find = new Model.Finder<>(String.class, Request.class);

    public static List<Request> getPendingRequests(String userId) {
        return find.where().eq("toUserId", userId).eq("status", Status.pending).findList();
    }

    public static enum Status {
        @EnumValue("accepted")
        accepted,
        @EnumValue("denied")
        denied,
        @EnumValue("pending")
        pending;
    }
}
