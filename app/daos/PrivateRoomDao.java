package daos;

import com.google.inject.ImplementedBy;
import models.entities.PrivateRoom;
import daos.impl.PrivateRoomDaoImpl;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(PrivateRoomDaoImpl.class)
public interface PrivateRoomDao extends GenericDao<PrivateRoom> {
    List<PrivateRoom> findByUserId(long userId);
    Optional<PrivateRoom> findBySenderAndReceiver(long senderId, long receiverId);
}