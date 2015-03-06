package controllers;

import models.entities.Request;
import models.entities.User;
import play.db.jpa.Transactional;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;


public class RequestsController extends BaseController {

    @Transactional
    public static Result createRequest() {
        return createWithObjects(Request.class);
    }

    @Transactional
    public static Result getRequests(long userId) {
        return okJson(Request.getPendingRequests(userId));
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
}
