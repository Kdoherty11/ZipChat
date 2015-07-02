package repositories.impl;

import models.entities.Message;
import play.db.jpa.JPA;
import repositories.MessageRepository;
import utils.DbUtils;

import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
public class MessageRepositoryImpl implements MessageRepository {

    @Override
    public Optional<Message> findById(long messageId) {
        return DbUtils.findEntityById(Message.class, messageId);
    }

    public List<Message> getMessages(long roomId, int limit, int offset) {

        String queryString = "select m from Message m where m.room.roomId = :roomId order by m.createdAt DESC";

        TypedQuery<Message> limitOffsetQuery = JPA.em().createQuery(queryString, Message.class)
                .setParameter("roomId", roomId)
                .setMaxResults(limit)
                .setFirstResult(offset);

        List<Message> messages = limitOffsetQuery.getResultList();

        Collections.reverse(messages);

        return messages;
    }
}
