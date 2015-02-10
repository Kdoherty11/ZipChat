package models;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.plugin.RedisPlugin;
import javafx.util.Callback;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import play.data.validation.Constraints;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;

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