package daos;

import com.google.inject.ImplementedBy;
import models.entities.AbstractRoom;
import daos.impl.AbstractRoomDaoImpl;

/**
 * Created by kdoherty on 7/4/15.
 */
@ImplementedBy(AbstractRoomDaoImpl.class)
public interface AbstractRoomDao extends GenericDao<AbstractRoom> {
}
