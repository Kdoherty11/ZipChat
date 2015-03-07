package controllers;

import models.entities.Request;
import play.db.jpa.Transactional;
import play.mvc.Result;


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
        return update(Request.class, requestId);
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
