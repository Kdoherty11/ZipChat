package utils;

import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Optional;

import static play.libs.Json.toJson;

/**
 * Created by kevindoherty on 2/24/15.
 */
public class DbUtils {

    private DbUtils() { }

    public static <T> Optional<T> findEntityById(Class<T> clazz, long id) {
        T entity = JPA.em().find(clazz, id);
        return Optional.ofNullable(entity);
    }

    public static <T> boolean deleteEntityById(Class<T> clazz, long id) {
        Optional<T> entityOptional = DbUtils.findEntityById(clazz, id);
        if (entityOptional.isPresent()) {
            JPA.em().remove(entityOptional.get());
            return true;
        } else {
            return false;
        }
    }

    public static Result getNotFoundResult(String entityName, long id) {
        return Controller.notFound(toJson(buildEntityNotFoundString(entityName, id)));
    }

    public static Result getNotFoundResult(Class clazz, long id) {
        return getNotFoundResult(clazz.getSimpleName(), id);
    }

    public static String buildEntityNotFoundString(String entityName, long id) {
        String errorMessage = entityName + " with id " + id + " was not found";
        Logger.warn(errorMessage);

        return errorMessage;
    }
}
