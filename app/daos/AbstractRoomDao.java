package daos;

import com.google.inject.ImplementedBy;
import daos.impl.AbstractRoomDaoImpl;
import models.entities.AbstractRoom;

/**
 * Created by kdoherty on 7/4/15.
 */
@ImplementedBy(AbstractRoomDaoImpl.class)
public interface AbstractRoomDao extends GenericDao<AbstractRoom> {
}
