package repositories;

import com.google.inject.ImplementedBy;
import models.entities.AbstractRoom;
import repositories.impl.AbstractRoomRepositoryImpl;

/**
 * Created by kdoherty on 7/4/15.
 */
@ImplementedBy(AbstractRoomRepositoryImpl.class)
public interface AbstractRoomRepository extends GenericRepository<AbstractRoom> {
}
