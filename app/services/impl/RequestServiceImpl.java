package services.impl;

import com.google.inject.Inject;
import models.entities.PrivateRoom;
import models.entities.Request;
import notifications.ChatResponseNotification;
import daos.PrivateRoomDao;
import daos.RequestDao;
import services.RequestService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 7/3/15.
 */
public class RequestServiceImpl extends GenericServiceImpl<Request> implements RequestService {

    private final RequestDao requestDao;
    private final PrivateRoomDao privateRoomDao;

    @Inject
    public RequestServiceImpl(final RequestDao requestDao, final PrivateRoomDao privateRoomDao) {
        super(requestDao);
        this.requestDao = requestDao;
        this.privateRoomDao = privateRoomDao;
    }

    @Override
    public List<Request> findPendingRequestsByReceiver(long receiverId) {
        return requestDao.findPendingRequestsByReceiver(receiverId);
    }

    @Override
    public String getStatus(long senderId, long receiverId) {
        return requestDao.getStatus(senderId, receiverId);
    }

    @Override
    public Optional<Request> findBySenderAndReceiver(long senderId, long receiverId) {
        return requestDao.findBySenderAndReceiver(senderId, receiverId);
    }

    @Override
    public void handleResponse(Request request, Request.Status status) {
        request.status = status;
        request.respondedTimeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        request.sender.sendNotification(new ChatResponseNotification(request, status));

        if (status == Request.Status.accepted) {
            PrivateRoom room = new PrivateRoom(request);
            privateRoomDao.save(room);
        }
    }
}
