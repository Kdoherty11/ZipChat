package daos.impl;

import daos.AbstractRoomDao;
import models.AbstractRoom;

/**
 * Created by kdoherty on 7/4/15.
 */
public class AbstractRoomDaoImpl extends GenericDaoImpl<AbstractRoom> implements AbstractRoomDao {
    public AbstractRoomDaoImpl() {
        super(AbstractRoom.class);
    }
}
