package factories;

/**
 * Created by kdoherty on 7/7/15.
 */
public class InMemoryIds {

    private static long roomId = 1;

    public static long getNextRoomId() {
        return roomId++;
    }
}
