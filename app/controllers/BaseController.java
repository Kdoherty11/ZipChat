package controllers;

import models.ForeignEntity;
import models.NoUpdate;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;
import play.Logger;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Yaml;
import play.mvc.Controller;
import play.mvc.Result;
import utils.DbUtils;

import javax.persistence.Id;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static play.data.Form.form;
import static play.libs.Json.toJson;

public class BaseController extends Controller {

    public static final String OK_STRING = "OK";
    public static final Result OK_RESULT = okJson(OK_STRING);

    public static final long INVALID_ID = -1L;

    protected static <T> Result create(Class<T> clazz) {
        Logger.debug("Creating a " + clazz.getSimpleName());

        Form<T> form = Form.form(clazz).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        } else {
            T entity = form.get();
            JPA.em().persist(entity);
            return okJson(entity);
        }
    }

    protected static <T> Result createWithForeignEntities(Class<T> clazz) {
        Logger.debug("Creating a " + clazz.getSimpleName() + " with foreign entities");
        Map<String, String> data = Form.form().bindFromRequest().data();

        T createdObject;
        try {
            createdObject = clazz.newInstance();
        } catch (InstantiationException e) {
            String error = clazz.getSimpleName() + " must have a no-arg constructor " + e.getMessage();
            Logger.error(error);
            return internalServerError(error);
        } catch (IllegalAccessException e) {
            String error = clazz.getSimpleName() + " must have a public no-arg-constructor " + e.getMessage();
            Logger.error(error);
            return internalServerError(error);
        }

        Field[] foreignEntityFields = Arrays.asList(clazz.getFields())
                .stream()
                .filter(field -> field.isAnnotationPresent(ForeignEntity.class))
                .toArray(Field[]::new);

        List<String> foreignEntityFieldNames = new ArrayList<>();

        for (Field field : foreignEntityFields) {
            String fieldName = field.getName();
            foreignEntityFieldNames.add(fieldName);

            if (data.containsKey(fieldName)) {
                long id = checkId(data.get(fieldName));
                if (INVALID_ID == id) {
                    return badRequestJson(fieldName + " must be a positive long");
                }

                Optional<?> entityOptional = DbUtils.findEntityById(field.getType(), id);
                if (entityOptional.isPresent()) {
                    try {
                        field.set(createdObject, entityOptional.get());
                    } catch (IllegalAccessException e) {
                        String error = "Not setting " + clazz.getSimpleName() + "." + fieldName + " because it is not visible";
                        Logger.error(error);
                        return internalServerError(error);
                    }
                } else {
                    return badRequestJson(DbUtils.buildEntityNotFoundString(field.getType().getSimpleName(), id));
                }
            } else {
                return badRequestJson(fieldName + " is required!");
            }
        }

        DataBinder dataBinder = new DataBinder(createdObject);
        dataBinder.setDisallowedFields(foreignEntityFieldNames.toArray(new String[foreignEntityFieldNames.size()]));
        dataBinder.bind(new MutablePropertyValues(data));

        JPA.em().persist(createdObject);
        return okJson(createdObject);
    }

    protected static <T> Result read(Class<T> clazz) {
        Logger.debug("Getting all " + clazz.getSimpleName() + "s");

        CriteriaQuery<T> cq = JPA.em().getCriteriaBuilder().createQuery(clazz);
        Root<T> root = cq.from(clazz);
        CriteriaQuery<T> all = cq.select(root);
        TypedQuery<T> allQuery = JPA.em().createQuery(all);

        return okJson(allQuery.getResultList());
    }

    protected static <T> Result update(Class<T> clazz, long id) {
        Logger.debug("Updating " + clazz.getSimpleName() + " with id " + id);

        Optional<T> entityOptional = DbUtils.findEntityById(clazz, id);

        if (entityOptional.isPresent()) {
            T entity = entityOptional.get();

            DataBinder dataBinder = new DataBinder(entity);
            dataBinder.setAllowedFields(getUpdateWhiteList(clazz));

            Map<String, String> newValues = form().bindFromRequest().data();
            dataBinder.bind(new MutablePropertyValues(newValues));

            return okJson(entity);
        } else {
            return badRequest(DbUtils.buildEntityNotFoundError(clazz, id));
        }
    }

    protected static <T> Result delete(Class<T> clazz, long id) {
        Logger.debug("Deleting " + clazz.getSimpleName() + " with id " + id);

        Optional<T> entityOptional = DbUtils.findEntityById(clazz, id);
        if (entityOptional.isPresent()) {
            JPA.em().remove(entityOptional.get());
            return OK_RESULT;
        } else {
            return badRequest(DbUtils.buildEntityNotFoundError(clazz, id));
        }
    }

    protected static <T> Result show(Class<T> clazz, long id) {
        Logger.debug("Showing " + clazz.getSimpleName() + " with id " + id);

        Optional<T> entityOptional = DbUtils.findEntityById(clazz, id);
        if (entityOptional.isPresent()) {
            return okJson(entityOptional.get());
        } else {
            return badRequest(DbUtils.buildEntityNotFoundError(clazz, id));
        }
    }

    protected static Result okJson(Object obj) {
        return ok(toJson(obj));
    }

    protected static Result badRequestJson(Object obj) {
        return badRequest(toJson(obj));
    }

    private static String[] getUpdateWhiteList(Class clazz) {
        return Arrays.asList(clazz.getDeclaredFields())
                .stream()
                .filter(BaseController::canBeUpdated)
                .map(Field::getName)
                .toArray(String[]::new);
    }

    private static boolean canBeUpdated(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(Id.class) && !field.isAnnotationPresent(NoUpdate.class);
    }

    public static long checkId(String id) {
        try {
            long longId = Long.valueOf(id);

            if (longId > 0) {
                return longId;
            } else {
                return INVALID_ID;
            }
        } catch (NumberFormatException e) {
            return INVALID_ID;
        }
    }

    @Transactional
    public static Result init() {
        @SuppressWarnings("unchecked")
        Map<String, List<Object>> all = (Map<String, List<Object>>) Yaml.load("seed_data.yml");
        all.get("users").forEach(user -> JPA.em().persist(user));
        all.get("rooms").forEach(room -> JPA.em().persist(room));
        all.get("messages").forEach(message -> JPA.em().persist(message));
        all.get("privateRooms").forEach(privateRoom -> JPA.em().persist(privateRoom));
        all.get("requests").forEach(request -> JPA.em().persist(request));
        return OK_RESULT;
    }
}
