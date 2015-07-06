package services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import daos.PrivateRoomDao;
import daos.RequestDao;
import daos.UserDao;
import models.Platform;
import models.entities.AbstractUser;
import models.entities.Device;
import models.entities.Request;
import models.entities.User;
import notifications.AbstractNotification;
import notifications.ChatRequestNotification;
import play.libs.ws.WSClient;
import services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by kdoherty on 7/3/15.
 */
public class UserServiceImpl extends GenericServiceImpl<User> implements UserService {

    private final UserDao userDao;
    private final RequestDao requestDao;
    private final PrivateRoomDao privateRoomDao;
    private final WSClient wsClient;

    @Inject
    public UserServiceImpl(final UserDao userDao, final RequestDao requestDao, final PrivateRoomDao privateRoomDao, final WSClient wsClient) {
        super(userDao);
        this.userDao = userDao;
        this.requestDao = requestDao;
        this.privateRoomDao = privateRoomDao;
        this.wsClient = wsClient;
    }

    @Override
    public void sendChatRequest(User sender, AbstractUser receiver) {
        User actualReceiver = receiver.getActual();
        if (!privateRoomDao.findBySenderAndReceiver(sender.userId, actualReceiver.userId).isPresent()) {
            requestDao.save(new Request(sender, actualReceiver));
            sendNotification(actualReceiver, new ChatRequestNotification(sender));
        }
    }

    @Override
    public void sendNotification(User receiver, AbstractNotification notification) {
        List<Device> devices = userDao.getDevices(receiver);
        if (devices.isEmpty()) {
            return;
        }

        List<String> androidRegIds = new ArrayList<>();
        List<String> iosRegIds = new ArrayList<>();

        for (Device info : devices) {
            if (info.platform == Platform.android) {
                androidRegIds.add(info.regId);
            } else {
                iosRegIds.add(info.regId);
            }
        }

        notification.send(androidRegIds, iosRegIds);
    }

    @Override
    public JsonNode getFacebookInformation(String fbAccessToken) {
        return wsClient.url("https://graph.facebook.com/me").setQueryParameter("access_token", fbAccessToken).get().get(5, TimeUnit.SECONDS).asJson();
    }

    @Override
    public Optional<User> findByFacebookId(String facebookId) {
        return userDao.findByFacebookId(facebookId);
    }

    @Override
    public List<Device> getDevices(User user) {
        return userDao.getDevices(user);
    }
}
