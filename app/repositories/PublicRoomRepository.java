package repositories;

import com.google.inject.ImplementedBy;
import models.entities.PublicRoom;
import repositories.impl.PublicRoomRepositoryImpl;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/29/15.
 */
@ImplementedBy(PublicRoomRepositoryImpl.class)
public interface PublicRoomRepository {

    public Optional<PublicRoom> findById(long roomId);
    public List<PublicRoom> allInGeoRange(double lat, double lon);

}
