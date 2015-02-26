package controllers;

import models.entities.Request;
import play.db.jpa.Transactional;
import play.mvc.Result;


public class RequestsController extends BaseController {

    @Transactional
    public static Result createRequest() {
        return create(Request.class);
    }

    @Transactional
    public static Result getRequests(String userId) {
        return okJson(Request.getPendingRequests(userId));
    }

    @Transactional
    public static Result showRequest(String requestId) {
        return show(Request.class, requestId);
    }

    @Transactional
    public static Result updateRequest(String requestId) {
        return update(Request.class, requestId);
    }

    @Transactional
    public static Result deleteRequest(String requestId) {
        return delete(Request.class, requestId);
    }
}
