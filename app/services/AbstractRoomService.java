package services;

import com.google.inject.ImplementedBy;
import daos.AbstractRoomDao;
import models.entities.AbstractRoom;
import models.entities.Message;
import services.impl.AbstractRoomServiceImpl;

import java.util.Set;

/**
 * Created by kdoherty on 7/4/15.
 */
@ImplementedBy(AbstractRoomServiceImpl.class)
public interface AbstractRoomService extends AbstractRoomDao {

    void addMessage(AbstractRoom room, Message message, Set<Long> userIdsInRoom);
}

