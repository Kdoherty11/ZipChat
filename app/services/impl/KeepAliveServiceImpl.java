package services.impl;

import akka.actor.Cancellable;
import play.api.Play;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import services.KeepAliveService;
import sockets.RoomSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by kdoherty on 8/7/15.
 */
public class KeepAliveServiceImpl implements KeepAliveService {

    // Seconds
    private static final long INITIAL_DELAY = 30;
    // Seconds
    private static final long INTERVAL = 30;

    private final Map<Long, Cancellable> keepAlives = new ConcurrentHashMap<>();

    @Override
    public void start(long roomId) {
        keepAlives.putIfAbsent(roomId, Play.current().actorSystem().scheduler().schedule(
                Duration.create(INITIAL_DELAY, SECONDS),
                Duration.create(INTERVAL, SECONDS),
                RoomSocket.defaultRoom,
                new RoomSocket.Talk(roomId, ID, MSG, ""),
                Akka.system().dispatcher(),
                null /** sender **/
        ));
    }

    @Override
    public void stop(long roomId) {
        Cancellable cancellable = keepAlives.get(roomId);
        if (cancellable != null && !cancellable.isCancelled()) {
            cancellable.cancel();
            keepAlives.remove(roomId);
        }
    }
}
