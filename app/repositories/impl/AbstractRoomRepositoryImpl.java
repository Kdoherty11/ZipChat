package repositories.impl;

import models.entities.AbstractRoom;
import repositories.AbstractRoomRepository;

/**
 * Created by kdoherty on 7/4/15.
 */
public class AbstractRoomRepositoryImpl extends GenericRepositoryImpl<AbstractRoom> implements AbstractRoomRepository {
    public AbstractRoomRepositoryImpl() {
        super(AbstractRoom.class);
    }
}
