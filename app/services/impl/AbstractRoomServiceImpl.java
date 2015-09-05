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

    public void addMessage(AbstractRoom room, Message message, Set<Long> excludedUserIds) {
        room.messages.add(message);
        room.lastActivity = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        if (room instanceof PublicRoom) {
            // Make sure anon messages don't send notifications to actual user
            excludedUserIds.add(message.sender.getActual().userId);
            publicRoomService.sendNotification((PublicRoom) room, new MessageNotification(message), excludedUserIds);
        } else {
            PrivateRoom privateRoom = (PrivateRoom) room;
            boolean isSender = privateRoom.sender.equals(message.sender);
            User notificationReceiver = isSender ? privateRoom.receiver : privateRoom.sender;
            boolean msgReceiverNotLeftRoom = isSender ? privateRoom.receiverInRoom : privateRoom.senderInRoom;
            if (msgReceiverNotLeftRoom && !excludedUserIds.contains(notificationReceiver.userId)) {
                userService.sendNotification(notificationReceiver, new MessageNotification(message));
            }
        }
    }

}
