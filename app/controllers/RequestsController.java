package controllers;

import models.entities.PrivateRoom;
import models.entities.Request;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;
import utils.DbUtils;

import java.util.Map;
import java.util.Optional;


public class RequestsController extends BaseController {

    @Transactional
    public static Result createRequest() {
        return createWithForeignEntities(Request.class);
    }

    @Transactional
    public static Result getRequests(long receiverId) {
        return okJson(Request.getPendingRequests(receiverId));
    }

    @Transactional
    public static Result showRequest(long requestId) {
        return show(Request.class, requestId);
    }

    @Transactional
    public static Result updateRequest(long requestId) {
        Map<String, String> formData = Form.form().bindFromRequest("status").data();
        String responseKey = "status";
        if (formData.containsKey(responseKey)) {
            Optional<Request> requestOptional = DbUtils.findEntityById(Request.class, requestId);
            if (requestOptional.isPresent()) {
                try {
                    Request.Status response = Request.Status.valueOf(formData.get(responseKey));
                    Optional<PrivateRoom> privateRoomOptional = requestOptional.get().handleResponse(response);

                    if (privateRoomOptional.isPresent()) {
                        return okJson(privateRoomOptional.get());
                    } else {
                        return OK_RESULT;
                    }

                } catch (IllegalArgumentException e) {
                    return badRequestJson(formData.get(responseKey) + " is not a valid response");
                }
            } else {
                return badRequestJson(DbUtils.buildEntityNotFoundError("Reqeust", requestId));
            }
        } else {
            return badRequestJson(responseKey + " is required");
        }
    }

    @Transactional
    public static Result deleteRequest(long requestId) {
        return delete(Request.class, requestId);
    }

    @Transactional
    public static Result doesExist(long senderId, long receiverId) {
        return okJson(Request.doesExist(senderId, receiverId));
    }
}
