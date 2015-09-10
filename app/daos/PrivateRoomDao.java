package daos;

import com.google.inject.ImplementedBy;
import daos.impl.PrivateRoomDaoImpl;
import models.PrivateRoom;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(PrivateRoomDaoImpl.class)
public interface PrivateRoomDao extends GenericDao<PrivateRoom> {
    List<PrivateRoom> findByUserId(long userId);
    Optional<PrivateRoom> findByActiveRoomMembers(long user1, long user2);
    Optional<PrivateRoom> findByRoomMembers(long user1, long user2);
}
