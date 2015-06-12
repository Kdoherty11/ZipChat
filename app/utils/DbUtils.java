package utils;

import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Result;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static play.libs.Json.toJson;

public class DbUtils {

    private DbUtils() { }

    public static <T> Optional<T> findEntityById(Class<T> clazz, long id) {
        T entity = JPA.em().find(clazz, id);
        return Optional.ofNullable(entity);
    }

    public static <T> T findExistingEntityById(Class<T> clazz, long id) {
        T entity = JPA.em().find(clazz, id);

        if (entity == null) {
            throw new EntityNotFoundException(buildEntityNotFoundString(clazz, id));
        }

        return entity;
    }

    public static Result getNotFoundResult(Class clazz, long id) {
        return Controller.notFound(toJson(buildEntityNotFoundString(clazz, id)));
    }

    public static String buildEntityNotFoundString(Class clazz, long id) {
        String errorMessage = clazz.getSimpleName() + " with id " + id + " was not found";
        Logger.warn(errorMessage);

        return errorMessage;
    }
}
