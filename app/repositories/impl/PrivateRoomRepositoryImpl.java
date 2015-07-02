package repositories.impl;

import models.entities.PrivateRoom;
import play.db.jpa.JPA;
import repositories.PrivateRoomRepository;
import utils.DbUtils;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
public class PrivateRoomRepositoryImpl implements PrivateRoomRepository {
    @Override
    public Optional<PrivateRoom> findById(long roomId) {
        return DbUtils.findEntityById(PrivateRoom.class, roomId);
    }

    @Override
    public List<PrivateRoom> findByUserId(long userId) {
        String queryString = "select p from PrivateRoom p where (p.sender.userId = :userId and p.senderInRoom = true) or (p.receiver.userId = :userId and p.receiverInRoom = true)";

        TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
                .setParameter("userId", userId);

        return query.getResultList();
    }

    @Override
    public Optional<PrivateRoom> findBySenderAndReceiver(long senderId, long receiverId) {
        String queryString = "select p from PrivateRoom p where " +
                "((p.sender.userId = :senderId and p.receiver.userId = :receiverId)" +
                " or (p.receiver.userId = :senderId and p.sender.userId = :receiverId))" +
                " and p.senderInRoom = true and p.receiverInRoom = true";

        TypedQuery<PrivateRoom> query = JPA.em().createQuery(queryString, PrivateRoom.class)
                .setParameter("senderId", senderId)
                .setParameter("receiverId", receiverId);

        List<PrivateRoom> rooms = query.getResultList();

        if (rooms.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(rooms.get(0));
    }
}
