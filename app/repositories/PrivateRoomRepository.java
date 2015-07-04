package repositories;

import com.google.inject.ImplementedBy;
import models.entities.PrivateRoom;
import repositories.impl.PrivateRoomRepositoryImpl;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(PrivateRoomRepositoryImpl.class)
public interface PrivateRoomRepository extends GenericRepository<PrivateRoom> {
    List<PrivateRoom> findByUserId(long userId);
    Optional<PrivateRoom> findBySenderAndReceiver(long senderId, long receiverId);
}
