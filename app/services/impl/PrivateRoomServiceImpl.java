package services.impl;

import com.google.inject.Inject;
import models.entities.PrivateRoom;
import daos.PrivateRoomDao;
import daos.RequestDao;
import services.PrivateRoomService;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 7/3/15.
 */
public class PrivateRoomServiceImpl extends GenericServiceImpl<PrivateRoom> implements PrivateRoomService {

    private final PrivateRoomDao privateRoomRepository;
    private final RequestDao requestRepository;

    @Inject
    public PrivateRoomServiceImpl(final PrivateRoomDao privateRoomRepository,
                                  final RequestDao requestRepository) {
        super(privateRoomRepository);
        this.privateRoomRepository = privateRoomRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public List<PrivateRoom> findByUserId(long userId) {
        return privateRoomRepository.findByUserId(userId);
    }

    @Override
    public Optional<PrivateRoom> findBySenderAndReceiver(long senderId, long receiverId) {
        return privateRoomRepository.findBySenderAndReceiver(senderId, receiverId);
    }

    @Override
    public boolean removeUser(PrivateRoom room, long userId) {
        if (userId == room.sender.userId) {
            if (!room.receiverInRoom) {
                privateRoomRepository.remove(room);
            } else {
                room.senderInRoom = false;
            }
        } else if (userId == room.receiver.userId) {
            if (!room.senderInRoom) {
                privateRoomRepository.remove(room);
            } else {
                room.receiverInRoom = false;
            }
        } else {
            return false;
        }

        // Allow both users to request each other again
        requestRepository.remove(room.request);
        return true;
    }
}
