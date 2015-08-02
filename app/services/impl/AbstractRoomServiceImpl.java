package services.impl;

import com.google.inject.Inject;
import daos.AbstractRoomDao;
import models.*;
import notifications.MessageNotification;
import services.AbstractRoomService;
import services.PublicRoomService;
import services.UserService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

/**
 * Created by kdoherty on 7/4/15.
 */
public class AbstractRoomServiceImpl extends GenericServiceImpl<AbstractRoom> implements AbstractRoomService {

    private final PublicRoomService publicRoomService;
    private final UserService userService;

    @Inject
    public AbstractRoomServiceImpl(AbstractRoomDao abstractRoomDao, PublicRoomService publicRoomService,
                                   UserService userService) {
        super(abstractRoomDao);
        this.publicRoomService = publicRoomService;
        this.userService = userService;
    }

    public void addMessage(AbstractRoom room, Message message, Set<Long> userIdsInRoom) {
        room.messages.add(message);
        room.lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        if (room instanceof PublicRoom) {
            publicRoomService.sendNotification((PublicRoom) room, new MessageNotification(message), userIdsInRoom);
        } else {
            PrivateRoom privateRoom = (PrivateRoom) room;
            User notificationReceiver = privateRoom.sender.equals(message.sender) ? privateRoom.receiver : privateRoom.sender;
            if (!userIdsInRoom.contains(notificationReceiver.userId)) {
                userService.sendNotification(notificationReceiver, new MessageNotification(message));
            }
        }
    }

}
