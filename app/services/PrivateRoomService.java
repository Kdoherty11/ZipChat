package services;

import com.google.inject.ImplementedBy;
import models.entities.PrivateRoom;
import repositories.PrivateRoomRepository;
import services.impl.PrivateRoomServiceImpl;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(PrivateRoomServiceImpl.class)
public interface PrivateRoomService extends PrivateRoomRepository {

    boolean removeUser(PrivateRoom room, long userId);

}
