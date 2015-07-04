package utils;

import play.Logger;
import play.db.jpa.JPA;

import javax.persistence.EntityNotFoundException;

public class DbUtils {

    private DbUtils() { }

    public static <T> T findExistingEntityById(Class<T> clazz, long id) {
        T entity = JPA.em().find(clazz, id);

        if (entity == null) {
            throw new EntityNotFoundException(buildEntityNotFoundString(clazz, id));
        }

        return entity;
    }

    public static String buildEntityNotFoundString(Class clazz, long id) {
        String errorMessage = clazz.getSimpleName() + " with id " + id + " was not found";
        Logger.warn(errorMessage);

        return errorMessage;
    }
}
