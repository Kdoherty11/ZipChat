package repositories;

import com.google.inject.ImplementedBy;
import models.entities.PublicRoom;
import models.entities.User;
import repositories.impl.PublicRoomRepositoryImpl;

import java.util.List;
import java.util.Set;

/**
 * Created by kdoherty on 6/29/15.
 */
@ImplementedBy(PublicRoomRepositoryImpl.class)
public interface PublicRoomRepository extends GenericRepository<PublicRoom> {
    List<PublicRoom> allInGeoRange(double lat, double lon);
    Set<User> getSubscribers(PublicRoom room);
}
