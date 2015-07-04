package services.impl;

import com.google.inject.Inject;
import models.Platform;
import models.entities.PublicRoom;
import models.entities.User;
import notifications.AbstractNotification;
import play.Logger;
import daos.PublicRoomDao;
import daos.UserDao;
import services.PublicRoomService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by kdoherty on 7/3/15.
 */
public class PublicRoomServiceImpl extends GenericServiceImpl<PublicRoom> implements PublicRoomService {

    private final PublicRoomDao publicRoomRepository;
    private final UserDao userRepository;

    @Inject
    public PublicRoomServiceImpl(final PublicRoomDao publicRoomRepository, final UserDao userRepository) {
        super(publicRoomRepository);
        this.publicRoomRepository = publicRoomRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<PublicRoom> allInGeoRange(double lat, double lon) {
        return publicRoomRepository.allInGeoRange(lat, lon);
    }

    @Override
    public Set<User> getSubscribers(PublicRoom room) {
        return publicRoomRepository.getSubscribers(room);
    }

    @Override
    public void sendNotification(PublicRoom room, AbstractNotification notification, Set<Long> userIdsInRoom) {
        Set<User> subscribers = getSubscribers(room);
        if (subscribers.isEmpty()) {
            return;
        }

        List<String> androidRegIds = new ArrayList<>();
        List<String> iosRegIds = new ArrayList<>();

        subscribers.forEach(user -> {
            if (!userIdsInRoom.contains(user.userId)) {
                Logger.debug("PublicRoom sendNotification to user " + user.userId + " with devices: " + user.devices);

                userRepository.getDevices(user).forEach(device -> {
                    if (device.platform == Platform.android) {
                        Logger.debug("Added android regId: " + device.regId);
                        androidRegIds.add(device.regId);
                    } else {
                        Logger.debug("Added ios regId: " + device.regId);
                        iosRegIds.add(device.regId);
                    }
                });
            }
        });

        Logger.debug("Sending to androidRegIds: " + androidRegIds + " and iosRegIds: " + iosRegIds);

        notification.send(androidRegIds, iosRegIds);
    }
}
