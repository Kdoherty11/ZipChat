package services.impl;

import com.google.inject.Inject;
import daos.PrivateRoomDao;
import daos.RequestDao;
import models.entities.PrivateRoom;
import services.PrivateRoomService;
import services.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 7/3/15.
 */
public class PrivateRoomServiceImpl extends GenericServiceImpl<PrivateRoom> implements PrivateRoomService {

    private final PrivateRoomDao privateRoomDao;
    private final RequestDao requestDao;
    private final UserService userService;

    @Inject
    public PrivateRoomServiceImpl(final PrivateRoomDao privateRoomDao, final RequestDao requestDao, final UserService userService) {
        super(privateRoomDao);
        this.privateRoomDao = privateRoomDao;
        this.requestDao = requestDao;
        this.userService = userService;
    }

    @Override
    public boolean removeUser(PrivateRoom room, long userId) {
        boolean roomRemoved = false;
        if (userId == room.sender.userId) {
            if (!room.receiverInRoom) {
                privateRoomDao.remove(room);
                roomRemoved = true;
            } else {
                room.senderInRoom = false;
            }
        } else if (userId == room.receiver.userId) {
            if (!room.senderInRoom) {
                privateRoomDao.remove(room);
                roomRemoved = true;
            } else {
                room.receiverInRoom = false;
            }
        } else {
            return false;
        }

        if (room.request != null) {
            // Allow both users to request each other again
            requestDao.remove(room.request);
            if (!roomRemoved) {
                room.request = null;
            }
        }

        return true;
    }

    @Override
    public boolean isUserInRoom(PrivateRoom room, long userId) {
        return (room.sender.userId == userId && room.senderInRoom) ||
                (room.receiver.userId == userId && room.receiverInRoom);
    }

    @Override
    public List<PrivateRoom> findByUserId(long userId) {
        return privateRoomDao.findByUserId(userId);
    }

    @Override
    public Optional<PrivateRoom> findBySenderAndReceiver(long senderId, long receiverId) {
        return privateRoomDao.findBySenderAndReceiver(senderId, receiverId);
    }
}
