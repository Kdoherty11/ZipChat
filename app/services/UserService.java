package services;

import com.google.inject.Inject;
import models.entities.AbstractUser;
import models.entities.Request;
import models.entities.User;
import notifications.ChatRequestNotification;
import repositories.PrivateRoomRepository;
import repositories.RequestRepository;
import repositories.UserRepository;

import java.util.Optional;

/**
 * Created by kdoherty on 7/1/15.
 */
public class UserService implements UserRepository {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final PrivateRoomRepository privateRoomRepository;

    @Inject
    public UserService(final UserRepository userRepository, final RequestRepository requestRepository, final PrivateRoomRepository privateRoomRepository) {
        this.userRepository = userRepository;
        this.requestRepository =requestRepository;
        this.privateRoomRepository = privateRoomRepository;
    }

    public void sendChatRequest(User sender, AbstractUser receiver) {
        User actualReceiver = receiver.getActual();
        if (!privateRoomRepository.findBySenderAndReceiver(sender.userId, actualReceiver.userId).isPresent()) {
            requestRepository.save(new Request(sender, actualReceiver));
            actualReceiver.sendNotification(new ChatRequestNotification(sender));
        }
    }

    @Override
    public Optional<User> findById(long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findByFacebookId(String facebookId) {
        return userRepository.findByFacebookId(facebookId);
    }
}
