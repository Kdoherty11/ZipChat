package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import models.NoUpdate;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Result;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.List;

import static play.libs.F.Promise;
import static play.libs.Json.toJson;


/**
 * Created by kevindoherty on 2/12/15.
 */

public class CrudUtils {

    public static String DELETE_SUCCESS_RESPONSE = "OK";

    private CrudUtils() {
    }

    public static <T extends Model> Promise<Result> create(Form<T> form, Callback cb) {
        if (form.hasErrors()) {
            return Promise.promise(() -> cb.failure(form.errorsAsJson()));
        } else {
            T entity = form.get();
            entity.save();
            return Promise.promise(() -> cb.success(toJson(entity)));
        }
    }

    public static <T extends Model> Promise<Result> read(Class<T> clazz, ReadCallback cb) {
        List<T> entities = new Model.Finder(String.class, clazz).all();
        return Promise.promise(() -> cb.success((toJson(entities))));
    }

    public static <T extends Model> Promise<Result> update(String id, Class<T> clazz, DynamicForm requestForm, Callback cb) {

        Promise<T> promiseEntity = Promise.promise(() -> new Model.Finder<>(String.class, clazz).byId(id));

        Promise<Result> resultPromise = promiseEntity.flatMap(entity -> {

            boolean updated = false;

            if (entity == null) {
                return Promise.promise(() -> cb.failure(toJson(getNotFoundError(id, clazz))));
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

        Promise<T> promiseEntity = Promise.promise(() -> new Model.Finder<>(String.class, clazz).byId(id));

        Promise<Result> resultPromise = promiseEntity.flatMap(entity -> {
            if (entity == null) {
                return Promise.promise(() -> cb.failure(getNotFoundError(id, clazz)));
            } else {
                entity.delete();
                return Promise.promise(() -> cb.success(toJson(DELETE_SUCCESS_RESPONSE)));
            }

        });

        return resultPromise;
    }

    public static <T extends Model> Promise<Result> show(String id, Class<T> clazz, Callback cb) {

        Promise<T> promiseEntity = Promise.promise(() -> new Model.Finder<>(String.class, clazz).byId(id));

        return promiseEntity.flatMap(entity -> {
            if (entity == null) {
                return Promise.promise(() -> cb.failure(getNotFoundError(id, clazz)));
            } else {
                return Promise.promise(() -> cb.success(toJson(entity)));
            }

        });
    }

    private static JsonNode getNotFoundError(String id, Class clazz) {
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getSimpleName());
        sb.append(" with id ");
        sb.append(id);
        sb.append(" was not found");

        return toJson(sb.toString());
    }

    private static boolean canBeUpdated(Field field) {
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
