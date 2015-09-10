package controllers;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import static play.libs.Json.toJson;

public abstract class BaseController extends Controller {

    public static final String OK_STRING = "OK";
    public static final Result OK_RESULT = okJson(OK_STRING);

    public static Result okJson(Object obj) {
        return ok(toJson(obj));
    }

    public static Result badRequestJson(Object obj) {
        return badRequest(toJson(obj));
    }

    public static String buildEntityNotFoundString(Class clazz, long id) {
        String errorMessage = clazz.getSimpleName() + " with id " + id + " was not found";
        Logger.warn(errorMessage);

        return errorMessage;
    }

    protected Result entityNotFound(Class clazz, long id) {
        return notFound(toJson(buildEntityNotFoundString(clazz, id)));
    }

//    Used for testing
//    ---------------
//    protected <T> Result read(Class<T> clazz) {
//        Logger.debug("Getting all " + clazz.getSimpleName() + "s");
//
//        CriteriaQuery<T> cq = JPA.em().getCriteriaBuilder().createQuery(clazz);
//        Root<T> root = cq.from(clazz);
//        CriteriaQuery<T> all = cq.select(root);
//        TypedQuery<T> allQuery = JPA.em().createQuery(all);
//
//        return okJson(allQuery.getResultList());
//    }
}
