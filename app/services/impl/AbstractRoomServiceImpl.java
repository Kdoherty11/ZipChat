package services.impl;

import com.google.inject.Inject;
import daos.AbstractRoomDao;
import models.entities.AbstractRoom;
import models.entities.Message;
import models.entities.PrivateRoom;
import models.entities.PublicRoom;
import notifications.MessageNotification;
import services.AbstractRoomService;
import services.PrivateRoomService;
import services.PublicRoomService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

/**
 * Created by kdoherty on 7/4/15.
 */
public class AbstractRoomServiceImpl extends GenericServiceImpl<AbstractRoom> implements AbstractRoomService {

    private final PublicRoomService publicRoomService;
    private final PrivateRoomService privateRoomService;

    @Inject
    public AbstractRoomServiceImpl(AbstractRoomDao abstractRoomDao, PublicRoomService publicRoomService,
                                   PrivateRoomService privateRoomService) {
        super(abstractRoomDao);
        this.publicRoomService = publicRoomService;
        this.privateRoomService = privateRoomService;
    }

    public void addMessage(AbstractRoom room, Message message, Set<Long> userIdsInRoom) {
        room.messages.add(message);
        room.lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        if (room instanceof PublicRoom) {
            publicRoomService.sendNotification((PublicRoom) room, new MessageNotification(message), userIdsInRoom);
        } else {
            privateRoomService.sendNotification((PrivateRoom) room, new MessageNotification(message), userIdsInRoom);
        }
    }

}
