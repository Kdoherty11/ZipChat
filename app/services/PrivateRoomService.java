package services;

import com.google.inject.Inject;
import models.entities.PrivateRoom;
import repositories.PrivateRoomRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 7/1/15.
 */
public class PrivateRoomService implements PrivateRoomRepository {

    private final PrivateRoomRepository privateRoomRepository;

    @Inject
    public PrivateRoomService(final PrivateRoomRepository privateRoomRepository) {
        this.privateRoomRepository = privateRoomRepository;
    }

    @Override
    public Optional<PrivateRoom> findById(long roomId) {
        return privateRoomRepository.findById(roomId);
    }

    @Override
    public List<PrivateRoom> findByUserId(long userId) {
        return privateRoomRepository.findByUserId(userId);
    }

    @Override
    public Optional<PrivateRoom> findBySenderAndReceiver(long senderId, long receiverId) {
        return privateRoomRepository.findBySenderAndReceiver(senderId, receiverId);
    }
}
