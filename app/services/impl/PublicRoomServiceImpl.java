package services.impl;

import com.google.inject.Inject;
import daos.PublicRoomDao;
import daos.UserDao;
import models.Platform;
import models.entities.PublicRoom;
import models.entities.User;
import notifications.AbstractNotification;
import services.PublicRoomService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by kdoherty on 7/3/15.
 */
public class PublicRoomServiceImpl extends GenericServiceImpl<PublicRoom> implements PublicRoomService {

    private final PublicRoomDao publicRoomDao;
    private final UserDao userDao;

    @Inject
    public PublicRoomServiceImpl(final PublicRoomDao publicRoomDao, final UserDao userDao) {
        super(publicRoomDao);
        this.publicRoomDao = publicRoomDao;
        this.userDao = userDao;
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

                userDao.getDevices(user).forEach(device -> {
                    if (device.platform == Platform.android) {
                        androidRegIds.add(device.regId);
                    } else {
                        iosRegIds.add(device.regId);
                    }
                });
            }
        });

        notification.send(androidRegIds, iosRegIds);
    }

    @Override
    public boolean subscribe(PublicRoom room, User user) {
        return room.subscribers.add(user);
    }

    @Override
    public boolean unsubscribe(PublicRoom room, User user) {
        return room.subscribers.remove(user);
    }

    @Override
    public boolean isSubscribed(PublicRoom room, long userId) {
        return room.subscribers.stream().anyMatch(user -> user.userId == userId);
    }

    @Override
    public List<PublicRoom> allInGeoRange(double lat, double lon) {
        return publicRoomDao.allInGeoRange(lat, lon);
    }

    @Override
    public Set<User> getSubscribers(PublicRoom room) {
        return publicRoomDao.getSubscribers(room);
    }



}
