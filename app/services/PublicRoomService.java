package services;

import com.google.inject.ImplementedBy;
import models.entities.PublicRoom;
import notifications.AbstractNotification;
import repositories.PublicRoomRepository;
import services.impl.PublicRoomServiceImpl;

import java.util.Set;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(PublicRoomServiceImpl.class)
public interface PublicRoomService extends PublicRoomRepository {

    void sendNotification(PublicRoom room, AbstractNotification notification, Set<Long> userIdsInRoom);

}
