package daos;

import com.google.inject.ImplementedBy;
import models.entities.PublicRoom;
import models.entities.User;
import daos.impl.PublicRoomDaoImpl;

import java.util.List;
import java.util.Set;

/**
 * Created by kdoherty on 6/29/15.
 */
@ImplementedBy(PublicRoomDaoImpl.class)
public interface PublicRoomDao extends GenericDao<PublicRoom> {
    List<PublicRoom> allInGeoRange(double lat, double lon);
    Set<User> getSubscribers(PublicRoom room);
}
