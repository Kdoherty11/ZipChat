package controllers;

import models.entities.Request;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Result;

import java.util.Map;

import static play.data.Form.form;


public class RequestsController extends BaseController {

    @Transactional
    public static Result createRequest() {

        Logger.debug("Creating a request");

        Map<String, String> data = form().bindFromRequest().data();

        String toUserKey = "toUserId";
        String fromUserKey = "fromUserId";

        if (!data.containsKey(toUserKey)) {
            return badRequestJson(toUserKey + " is required");
        }

        if (!data.containsKey(fromUserKey)) {
            return badRequestJson(fromUserKey + " is required");
        }

        Request request = new Request(data.get(fromUserKey), data.get(toUserKey));
        JPA.em().persist(request);

        return okJson(request);
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
