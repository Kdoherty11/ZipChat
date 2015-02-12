package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.plugin.RedisPlugin;
import models.ChatRoom;
import models.ChatRoomModel;
import play.Logger;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import views.html.index;

import java.util.List;

import static play.libs.Json.toJson;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    /**
     * Handle the chat websocket.
     */
    public static WebSocket<JsonNode> chat(final String roomId, final String username) {
        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){

                // Join the chat room.
                try {
                    ChatRoom.join(roomId, username, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public static Result  test() {
        System.out.println("I am here");
        Logger.debug("Debug log");

        try {

            play.Application app = play.Play.application();
            Logger.debug("app: " + app);
            Object plugin = app.plugin(RedisPlugin.class);
            Logger.debug("plugin: " + plugin);

            Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();

            Logger.info("Success init Jedis!!!!");
        } catch (Exception e) {
            Logger.error("Jedis init error: " + e);
        }


        return ok(toJson("Ok Json"));

    }

}
