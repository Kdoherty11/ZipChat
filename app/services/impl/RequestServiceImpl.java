package services.impl;

import com.google.inject.Inject;
import daos.PrivateRoomDao;
import daos.RequestDao;
import models.PrivateRoom;
import models.Request;
import notifications.ChatResponseNotification;
import play.libs.Json;
import services.RequestService;
import services.UserService;

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
    private final UserService userService;

    @Inject
    public RequestServiceImpl(final RequestDao requestDao, final PrivateRoomDao privateRoomDao, final UserService userService) {
        super(requestDao);
        this.requestDao = requestDao;
        this.privateRoomDao = privateRoomDao;
        this.userService = userService;
    }

    @Override
    public void handleResponse(Request request, Request.Status status) {
        request.status = status;
        request.respondedTimeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        userService.sendNotification(request.sender, new ChatResponseNotification(request, status));

        if (status == Request.Status.accepted) {
            Optional<PrivateRoom> existingRoom = privateRoomDao.findByRoomMembers(request.sender.userId, request.receiver.userId);
            if (existingRoom.isPresent()) {
                PrivateRoom existing = existingRoom.get();
                existing.senderInRoom = true;
                existing.receiverInRoom = true;
            } else {
                PrivateRoom room = new PrivateRoom(request);
                privateRoomDao.save(room);
            }
        }
    }

    @Override
    public String getStatus(long potentialSenderId, long potentialReceiverId) {
        Optional<PrivateRoom> privateRoomOptional = privateRoomDao.findByActiveRoomMembers(potentialSenderId, potentialReceiverId);

        if (privateRoomOptional.isPresent()) {
            return Json.stringify(Json.toJson(privateRoomOptional.get()));
        }

        Optional<Request> requestOptional = findByUsers(potentialSenderId, potentialReceiverId);
        if (requestOptional.isPresent()) {
            Request request = requestOptional.get();
            if (request.sender.userId == potentialSenderId) {
                return requestOptional.get().status.name();
            } else if (request.status == Request.Status.pending){
                return Request.Status.pending.name();
            }
        }

        return "none";
    }

    @Override
    public List<Request> findPendingRequestsByReceiver(long receiverId) {
        return requestDao.findPendingRequestsByReceiver(receiverId);
    }

    @Override
    public Optional<Request> findByUsers(long userId1, long userId2) {
        return requestDao.findByUsers(userId1, userId2);
    }


}
