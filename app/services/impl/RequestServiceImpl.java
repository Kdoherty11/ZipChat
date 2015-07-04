package services.impl;

import com.google.inject.Inject;
import models.entities.PrivateRoom;
import models.entities.Request;
import notifications.ChatResponseNotification;
import repositories.PrivateRoomRepository;
import repositories.RequestRepository;
import services.RequestService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 7/3/15.
 */
public class RequestServiceImpl extends GenericServiceImpl<Request> implements RequestService {

    private final RequestRepository requestRepository;
    private final PrivateRoomRepository privateRoomRepository;

    @Inject
    public RequestServiceImpl(final RequestRepository requestRepository, final PrivateRoomRepository privateRoomRepository) {
        super(requestRepository);
        this.requestRepository = requestRepository;
        this.privateRoomRepository = privateRoomRepository;
    }

    @Override
    public List<Request> findPendingRequestsByReceiver(long receiverId) {
        return requestRepository.findPendingRequestsByReceiver(receiverId);
    }

    @Override
    public String getStatus(long senderId, long receiverId) {
        return requestRepository.getStatus(senderId, receiverId);
    }

    @Override
    public Optional<Request> findBySenderAndReceiver(long senderId, long receiverId) {
        return requestRepository.findBySenderAndReceiver(senderId, receiverId);
    }

    @Override
    public void handleResponse(Request request, Request.Status status) {
        request.status = status;
        request.respondedTimeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        request.sender.sendNotification(new ChatResponseNotification(request, status));

        if (status == Request.Status.accepted) {
            PrivateRoom room = new PrivateRoom(request);
            privateRoomRepository.save(room);
        }
    }
}
