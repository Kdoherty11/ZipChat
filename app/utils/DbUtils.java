package utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.db.jpa.JPA;

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

    public static JsonNode buildEntityNotFoundError(Class clazz, long id) {
        return buildEntityNotFoundError(clazz.getSimpleName(), id);
    }

    public static JsonNode buildEntityNotFoundError(String entityName, long id) {
        return toJson(buildEntityNotFoundString(entityName, id));
    }

    public static String buildEntityNotFoundString(Class clazz, long id) {
        return buildEntityNotFoundString(clazz.getSimpleName(), id);
    }

    public static String buildEntityNotFoundString(String entityName, long id) {
        String errorMessage = entityName + " with id " + id + " was not found";
        Logger.warn(errorMessage);

        return errorMessage;
    }
}
