package services.impl;

import com.google.inject.Inject;
import daos.PrivateRoomDao;
import daos.RequestDao;
import models.PrivateRoom;
import play.Logger;
import services.PrivateRoomService;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 7/3/15.
 */
public class PrivateRoomServiceImpl extends GenericServiceImpl<PrivateRoom> implements PrivateRoomService {

    private final PrivateRoomDao privateRoomDao;
    private final RequestDao requestDao;

    @Inject
    public PrivateRoomServiceImpl(final PrivateRoomDao privateRoomDao, final RequestDao requestDao) {
        super(privateRoomDao);
        this.privateRoomDao = privateRoomDao;
        this.requestDao = requestDao;
    }

    @Override
    public boolean removeUser(PrivateRoom room, long userId) {
        if (userId == room.sender.userId) {
            if (!room.receiverInRoom) {
                privateRoomDao.remove(room);
            } else {
                room.senderInRoom = false;
            }
        } else if (userId == room.receiver.userId) {
            if (!room.senderInRoom) {
                privateRoomDao.remove(room);
            } else {
                room.receiverInRoom = false;
            }
        } else {
            return false;
        }

        if (room.request != null) {
            // Allow both users to request each other again
            requestDao.remove(room.request);
            room.request = null;
        }

        Logger.info("Success!!");

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
    public Optional<PrivateRoom> findByRoomMembers(long senderId, long receiverId) {
        return privateRoomDao.findByRoomMembers(senderId, receiverId);
    }
}
