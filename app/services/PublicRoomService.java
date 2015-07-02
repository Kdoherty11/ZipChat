package services;

import com.google.inject.Inject;
import models.entities.PublicRoom;
import repositories.PublicRoomRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 7/1/15.
 */
public class PublicRoomService implements PublicRoomRepository {

    private final PublicRoomRepository publicRoomRepository;

    @Inject
    public PublicRoomService(final PublicRoomRepository publicRoomRepository) {
        this.publicRoomRepository = publicRoomRepository;
    }

    @Override
    public Optional<PublicRoom> findById(long roomId) {
        return publicRoomRepository.findById(roomId);
    }

    @Override
    public List<PublicRoom> allInGeoRange(double lat, double lon) {
        return publicRoomRepository.allInGeoRange(lat, lon);
    }
}
