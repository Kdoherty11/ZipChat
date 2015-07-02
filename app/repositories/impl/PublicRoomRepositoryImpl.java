package repositories.impl;

import models.entities.PublicRoom;
import play.db.jpa.JPA;
import repositories.PublicRoomRepository;
import utils.DbUtils;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/29/15.
 */
public class PublicRoomRepositoryImpl implements PublicRoomRepository {

    @Override
    public Optional<PublicRoom> findById(long roomId) {
        return DbUtils.findEntityById(PublicRoom.class, roomId);
    }

    @Override
    public List<PublicRoom> allInGeoRange(double lat, double lon) {
        int earthRadius = 6371; // in km

        String firstCutSql = "select r2.roomId" +
                " from PublicRoom r2" +
                " where :lat >= r2.latitude - degrees((r2.radius * 1000) / :R) and :lat <= r2.latitude + degrees((r2.radius * 1000) / :R)" +
                " and :lon >= r2.longitude - degrees((r2.radius * 1000) / :R) and :lon <= r2.longitude + degrees((r2.radius * 1000) / :R)";

        String sql = "select r" +
                " from PublicRoom r" +
                " where r.roomId in (" + firstCutSql + ") and" +
                " acos(sin(radians(:lat)) * sin(radians(latitude)) + cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lon))) * :R * 1000 <= radius";

        TypedQuery<PublicRoom> query = JPA.em().createQuery(sql, PublicRoom.class)
                .setParameter("lat", lat)
                .setParameter("lon", lon)
                .setParameter("R", earthRadius);

        return query.getResultList();
    }
}
