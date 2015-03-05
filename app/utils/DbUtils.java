package utils;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;

import java.util.Optional;

import static play.libs.Json.toJson;

/**
 * Created by kevindoherty on 2/24/15.
 */
public class DbUtils {

    private DbUtils() { }

    @Transactional
    public static <T> Optional<T> findEntityById(Class<T> clazz, long id) {
        T entity = JPA.em().find(clazz, id);
        return Optional.ofNullable(entity);
    }

    public static JsonNode buildEntityNotFoundError(Class clazz, long id) {
        return buildEntityNotFoundError(clazz.getSimpleName(), id);
    }

    public static JsonNode buildEntityNotFoundError(String entityName, long id) {
        String errorMessage = buildEntityNotFoundString(entityName, id);

        Logger.warn(errorMessage);

        return toJson(errorMessage);
    }

    public static String buildEntityNotFoundString(String entityName, long id) {
        return entityName + " with userId " + id + " was not found";
    }
}
