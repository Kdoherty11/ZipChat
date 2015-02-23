package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import utils.CrudUtils;
import models.entities.Request;
import play.mvc.Result;

import java.util.List;

import static play.data.Form.form;
import static play.libs.F.Promise;
import static play.libs.Json.toJson;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;


public class RequestsController {

    private static final CrudUtils.Callback DEFAULT_CB = new CrudUtils.Callback() {
        @Override
        public Result success(JsonNode entity) {
            return ok(entity);
        }

        @Override
        public Result failure(JsonNode error) {
            return badRequest(error);
        }
    };

    public static Promise<Result> createRequest() {
        return CrudUtils.create(form(Request.class).bindFromRequest(), DEFAULT_CB);
    }

    public static Promise<Result> getRequests(String userId) {
        Promise<List<Request>> promiseRequests = Promise.promise(() -> Request.getPendingRequests(userId));
        return promiseRequests.flatMap(entities -> Promise.promise(() -> ok(toJson(entities))));
    }

    public static Promise<Result> showRequest(String requestId) {
        return CrudUtils.show(requestId, Request.class, DEFAULT_CB);
    }

    public static Promise<Result> updateRequest(String requestId) {
        return CrudUtils.update(requestId, Request.class, form().bindFromRequest(), DEFAULT_CB);
    }

    public static Promise<Result> deleteRequest(String requestId) {
        return CrudUtils.delete(requestId, Request.class, DEFAULT_CB);
    }
}
