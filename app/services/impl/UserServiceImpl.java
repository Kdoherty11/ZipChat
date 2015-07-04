package services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Platform;
import models.entities.AbstractUser;
import models.entities.Device;
import models.entities.Request;
import models.entities.User;
import notifications.AbstractNotification;
import notifications.ChatRequestNotification;
import play.libs.ws.WS;
import repositories.PrivateRoomRepository;
import repositories.RequestRepository;
import repositories.UserRepository;
import services.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by kdoherty on 7/3/15.
 */
public class UserServiceImpl extends GenericServiceImpl<User> implements UserService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final PrivateRoomRepository privateRoomRepository;

    @Inject
    public UserServiceImpl(final UserRepository userRepository, final RequestRepository requestRepository, final PrivateRoomRepository privateRoomRepository) {
        super(userRepository);
        this.userRepository = userRepository;
        this.requestRepository =requestRepository;
        this.privateRoomRepository = privateRoomRepository;
    }

    @Override
    public void sendChatRequest(User sender, AbstractUser receiver) {
        User actualReceiver = receiver.getActual();
        if (!privateRoomRepository.findBySenderAndReceiver(sender.userId, actualReceiver.userId).isPresent()) {
            requestRepository.save(new Request(sender, actualReceiver));
            actualReceiver.sendNotification(new ChatRequestNotification(sender));
        }
    }

    @Override
    public void sendNotification(User receiver, AbstractNotification notification) {
        List<Device> devices = userRepository.getDevices(receiver);
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
        return WS.url("https://graph.facebook.com/me").setQueryParameter("access_token", fbAccessToken).get().get(5, TimeUnit.SECONDS).asJson();

    }

    @Override
    public Optional<User> findByFacebookId(String facebookId) {
        return userRepository.findByFacebookId(facebookId);
    }

    @Override
    public List<Device> getDevices(User user) {
        return userRepository.getDevices(user);
    }
}
