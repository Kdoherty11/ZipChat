package controllers;

import play.Logger;
import play.Play;
import play.data.Form;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.Secured;
import utils.DbUtils;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import static play.libs.Json.toJson;

public class BaseController extends Controller {

    public static final String OK_STRING = "OK";
    public static final Result OK_RESULT = okJson(OK_STRING);

    public static long getTokenUserId() {
        return Play.isProd() ? (long) Http.Context.current().args.get(Secured.USER_ID_KEY) : 1;
    }

    public static boolean isUnauthorized(long userId) {
        return userId != getTokenUserId() && Play.isProd();
    }

    public static Result okJson(Object obj) {
        return ok(toJson(obj));
    }

    public static Result badRequestJson(Object obj) {
        return badRequest(toJson(obj));
    }

    // Pinged to check server status
    public Result status() {
        return OK_RESULT;
    }

    public static <T> Result create(Class<T> clazz) {
        Logger.debug("Creating a " + clazz.getSimpleName());

        Form<T> form = Form.form(clazz).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        } else {
            T entity = form.get();
            JPA.em().persist(entity);
            return created(toJson(entity));
        }
    }

    public static <T> Result read(Class<T> clazz) {
        Logger.debug("Getting all " + clazz.getSimpleName() + "s");

        CriteriaQuery<T> cq = JPA.em().getCriteriaBuilder().createQuery(clazz);
        Root<T> root = cq.from(clazz);
        CriteriaQuery<T> all = cq.select(root);
        TypedQuery<T> allQuery = JPA.em().createQuery(all);

        return okJson(allQuery.getResultList());
    }

    public static Result entityNotFound(Class clazz, long id) {
        return notFound(toJson(DbUtils.buildEntityNotFoundString(clazz, id)));
    }

//    public static long getId(Object entity) {
//        try {
//            return Arrays.asList(entity.getClass().getFields())
//                    .stream()
//                    .filter(field -> field.isAnnotationPresent(Id.class))
//                    .findFirst()
//                    .orElseThrow(() -> new RuntimeException("No ID for entity " + entity))
//                    .getLong(entity);
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
