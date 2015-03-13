package controllers;

import models.entities.Request;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;
import utils.DbUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class RequestsController extends BaseController {

    @Transactional
    public static Result createRequest() {
        return createWithForeignEntities(Request.class, createdRequest -> {
            Map<String, String> notificationData = new HashMap<>();
            notificationData.put("event", "request");
            notificationData.put("senderName", createdRequest.sender.name);
            notificationData.put("senderFbId", String.valueOf(createdRequest.sender.userId));

            createdRequest.receiver.sendNotification(notificationData);
        });
    }

    @Transactional
    public static Result getRequests(long receiverId) {
        return okJson(Request.getPendingRequestsByReceiver(receiverId));
    }

    @Transactional
    public static Result handleResponse(long requestId) {
        String responseKey = "status";

        Map<String, String> formData = Form.form().bindFromRequest(responseKey).data();
        if (formData.containsKey(responseKey)) {
            Request.Status response;
            try {
                response = Request.Status.valueOf(formData.get(responseKey));
            } catch (IllegalArgumentException e) {
                return badRequestJson(formData.get(responseKey) + " is not a valid response");
            }

            if (Request.Status.pending == response) {
                return badRequestJson("Can't respond to a request with pending");
            }

            Optional<Request> requestOptional = DbUtils.findEntityById(Request.class, requestId);
            if (requestOptional.isPresent()) {
                Request request = requestOptional.get();
                request.status = response;
                request.respondedTimeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

                request.handleResponse(response);

                return OK_RESULT;
            } else {
                return badRequestJson(DbUtils.buildEntityNotFoundError("Request", requestId));
            }
        } else {
            return badRequestJson(responseKey + " is required");
        }
    }

    @Transactional
    public static Result canSendRequest(long senderId, long receiverId) {
        return okJson(Request.canSendRequest(senderId, receiverId));
    }
}
