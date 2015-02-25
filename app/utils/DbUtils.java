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
        String errorMessage = entityName + " with id " + id + " was not found";

        Logger.warn(errorMessage);

        return toJson(errorMessage);
    }
}
