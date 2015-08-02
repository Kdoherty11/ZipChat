package services;

import com.google.inject.ImplementedBy;
import daos.PrivateRoomDao;
import models.PrivateRoom;
import services.impl.PrivateRoomServiceImpl;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(PrivateRoomServiceImpl.class)
public interface PrivateRoomService extends PrivateRoomDao {

    boolean removeUser(PrivateRoom room, long userId);
    boolean isUserInRoom(PrivateRoom room, long userId);
}
