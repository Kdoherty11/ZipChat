package models;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.WebSocket;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;

public class SocketKeepAlive {

    public static final long USER_ID = -10;
    public static final String HEARTBEAT_MESSAGE = "Beat";

    Cancellable cancellable;
    long roomId;

    public SocketKeepAlive(long roomId, ActorRef chatRoom) {

        // Create a Fake socket out for the robot that log events to the console.
        WebSocket.Out<JsonNode> robotChannel = new WebSocket.Out<JsonNode>() {

            public void write(JsonNode frame) {
                Logger.debug("heartbeat write");
                Logger.of("heartbeat").info(Json.stringify(frame));
            }

            public void close() {}

        };

        // Join the room
        chatRoom.tell(new RoomSocket.Join(roomId, USER_ID, robotChannel), null);

        this.roomId = roomId;

        // Make the robot talk every 30 seconds
        cancellable = Akka.system().scheduler().schedule(
                Duration.create(30, SECONDS),
                Duration.create(30, SECONDS),
                chatRoom,
                new RoomSocket.Talk(roomId, USER_ID, HEARTBEAT_MESSAGE),
                Akka.system().dispatcher(),
                /** sender **/ null
        );

    }

    public void stop() {
        Logger.debug("Stopped heartbeat with roomId " + roomId);
        if (cancellable != null && !cancellable.isCancelled()) {
            cancellable.cancel();
        }
    }

}