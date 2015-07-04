package services.impl;

import models.entities.AbstractRoom;
import daos.AbstractRoomDao;
import services.AbstractRoomService;

/**
 * Created by kdoherty on 7/4/15.
 */
public class AbstractRoomServiceImpl extends GenericServiceImpl<AbstractRoom> implements AbstractRoomService {

    public AbstractRoomServiceImpl(AbstractRoomDao abstractRoomDao) {
        super(abstractRoomDao);
    }
}
