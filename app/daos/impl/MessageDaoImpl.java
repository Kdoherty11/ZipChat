package daos.impl;

import daos.MessageDao;
import models.Message;
import play.db.jpa.JPA;

import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

/**
 * Created by kdoherty on 6/30/15.
 */
public class MessageDaoImpl extends GenericDaoImpl<Message> implements MessageDao {

    public MessageDaoImpl() {
        super(Message.class);
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
