package services;

import com.google.inject.ImplementedBy;
import daos.PublicRoomDao;
import models.entities.PublicRoom;
import models.entities.User;
import notifications.AbstractNotification;
import services.impl.PublicRoomServiceImpl;

import java.util.Set;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(PublicRoomServiceImpl.class)
public interface PublicRoomService extends PublicRoomDao {

    void sendNotification(PublicRoom room, AbstractNotification notification, Set<Long> userIdsInRoom);
    boolean subscribe(PublicRoom room, User user);
    boolean unsubscribe(PublicRoom room, User user);
    boolean isSubscribed(PublicRoom room, long userId);
}