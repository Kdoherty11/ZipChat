package models;

import play.*;
import play.mvc.*;
import play.libs.*;

import scala.concurrent.duration.*;
import akka.actor.*;

import com.fasterxml.jackson.databind.JsonNode;

import static java.util.concurrent.TimeUnit.*;

public class Robot {

    Cancellable cancellable;
    String roomId;

    public Robot(String roomId, ActorRef chatRoom) {

        // Create a Fake socket out for the robot that log events to the console.
        WebSocket.Out<JsonNode> robotChannel = new WebSocket.Out<JsonNode>() {

            public void write(JsonNode frame) {
                Logger.debug("robot write");
                Logger.of("robot").info(Json.stringify(frame));
            }

            public void close() {}

        };

        // Join the room
        chatRoom.tell(new ChatRoom.Join(roomId, "Robot", robotChannel), null);
        Logger.debug("Robot joined");

        this.roomId = roomId;

        // Make the robot talk every 30 seconds
        cancellable = Akka.system().scheduler().schedule(
                Duration.create(30, SECONDS),
                Duration.create(30, SECONDS),
                chatRoom,
                new ChatRoom.Talk(roomId, "Robot", "I'm still alive"),
                Akka.system().dispatcher(),
                /** sender **/ null
        );

    }

    public void stop() {
        Logger.debug("Stopped robot with roomId " + roomId);
        if (cancellable != null && !cancellable.isCancelled()) {
            cancellable.cancel();
        }
    }

}