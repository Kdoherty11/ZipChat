package services;

import com.google.inject.ImplementedBy;
import daos.PrivateRoomDao;
import models.entities.PrivateRoom;
import notifications.AbstractNotification;
import services.impl.PrivateRoomServiceImpl;

import java.util.Set;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(PrivateRoomServiceImpl.class)
public interface PrivateRoomService extends PrivateRoomDao {

    boolean removeUser(PrivateRoom room, long userId);
    boolean isUserInRoom(PrivateRoom room, long userId);
    void sendNotification(PrivateRoom room, AbstractNotification notification, Set<Long> userIdsInRoom);

}
