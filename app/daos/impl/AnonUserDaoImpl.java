package daos.impl;

import daos.AnonUserDao;
import models.entities.AnonUser;
import models.entities.PublicRoom;
import models.entities.User;
import play.db.jpa.JPA;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 7/4/15.
 */
public class AnonUserDaoImpl extends GenericDaoImpl<AnonUser> implements AnonUserDao {

    public AnonUserDaoImpl() {
        super(AnonUser.class);
    }

    @Override
    public Optional<AnonUser> getAnonUser(User actual, PublicRoom room) {
        String queryString = "select a from AnonUser a where a.actual.userId = :userId and a.room.roomId = :roomId";

        TypedQuery<AnonUser> query = JPA.em().createQuery(queryString, AnonUser.class)
                .setParameter("userId", actual.userId)
                .setParameter("roomId", room.roomId);

        List<AnonUser> aliases = query.getResultList();
        if (aliases.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(aliases.get(0));
        }
    }
}
