package utils;


import com.fasterxml.jackson.databind.JsonNode;
import models.NoUpdate;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Result;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.List;

import static play.libs.F.Promise;
import static play.libs.Json.toJson;


public class CrudUtils {

    public static String DELETE_SUCCESS_RESPONSE = "OK";

    private CrudUtils() {
    }

    public static <T extends Model> Promise<Result> create(Form<T> form, Callback cb) {

        Logger.debug("Creating a " + form.get().getClass().getSimpleName());

        if (form.hasErrors()) {
            return Promise.promise(() -> cb.failure(form.errorsAsJson()));
        } else {
            T entity = form.get();
            entity.save();
            return Promise.promise(() -> cb.success(toJson(entity)));
        }
    }

    public static <T extends Model> Promise<Result> read(Class<T> clazz, ReadCallback cb) {

        Logger.debug("Getting all " + clazz.getSimpleName() + "s");

        Promise<List<T>> promiseEntities = Promise.promise(() -> new Model.Finder<>(String.class, clazz).all());
        return promiseEntities.flatMap(entities -> Promise.promise(() -> cb.success(toJson(entities))));
    }

    public static <T extends Model> Promise<Result> update(String id, Class<T> clazz, DynamicForm requestForm, Callback cb) {

        Logger.debug("Updating " + clazz.getSimpleName() + " with id " + id);

        Promise<T> promiseEntity = Promise.promise(() -> new Model.Finder<>(String.class, clazz).byId(id));

        Promise<Result> resultPromise = promiseEntity.flatMap(entity -> {

            boolean updated = false;

            if (entity == null) {
                return Promise.promise(() -> cb.failure(toJson(buildEntityNotFoundError(clazz, id))));
            }

            Field[] modelFields = clazz.getDeclaredFields();
            int fieldIndex = 0;

            for (Field field : modelFields) {
                fieldIndex++;

                if (canBeUpdated(field)) {

                    String updateFieldValue = requestForm.get(field.getName());

                    // The field is included in the update request
                    if (updateFieldValue != null) {
                        entity._ebean_setField(fieldIndex, entity, updateFieldValue);
                        updated = true;
                    }
                }
            }

            if (updated) {
                entity.update();
            }

            return Promise.promise(() -> cb.success(toJson(entity)));
        });

        return resultPromise;
    }

    public static <T extends Model> Promise<Result> delete(String id, Class<T> clazz, Callback cb) {

        Logger.debug("Deleting " + clazz.getSimpleName() + " with id " + id);

        Promise<T> promiseEntity = Promise.promise(() -> new Model.Finder<>(String.class, clazz).byId(id));

        Promise<Result> resultPromise = promiseEntity.flatMap(entity -> {
            if (entity == null) {
                return Promise.promise(() -> cb.failure(buildEntityNotFoundError(clazz, id)));
            } else {
                entity.delete();
                return Promise.promise(() -> cb.success(toJson(DELETE_SUCCESS_RESPONSE)));
            }

        });

        return resultPromise;
    }

    public static <T extends Model> Promise<Result> show(String id, Class<T> clazz, Callback cb) {

        Logger.debug("Showing " + clazz.getSimpleName() + " with id " + id);

        Promise<T> promiseEntity = Promise.promise(() -> new Model.Finder<>(String.class, clazz).byId(id));

        return promiseEntity.flatMap(entity -> {
            if (entity == null) {
                return Promise.promise(() -> cb.failure(buildEntityNotFoundError(clazz, id)));
            } else {
                return Promise.promise(() -> cb.success(toJson(entity)));
            }

        });
    }

    public static JsonNode buildEntityNotFoundError(Class clazz, String id) {
        return buildEntityNotFoundError(clazz.getSimpleName(), id);
    }

    public static JsonNode buildEntityNotFoundError(String entityName, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append(entityName);
        sb.append(" with id ");
        sb.append(id);
        sb.append(" was not found");

        String result = sb.toString();

        Logger.warn(result);

        return toJson(result);
    }

    public static boolean canBeUpdated(Field field) {
        return !field.isAnnotationPresent(Id.class) && !field.isAnnotationPresent(NoUpdate.class);
    }

    public interface ReadCallback {
        public Result success(JsonNode entities);
    }

    public interface Callback {
        public Result success(JsonNode entity);
        public Result failure(JsonNode error);
    }

}
