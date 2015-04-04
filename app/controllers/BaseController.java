package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.ForeignEntity;
import models.NoUpdate;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints;
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

    public interface Callback<T> {
        void success(T createdEntity);
    }

    protected static <T> Result create(Class<T> clazz) {
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

    protected static <T> Result createWithForeignEntities(Class<T> clazz) {
        return createWithForeignEntities(clazz, null);
    }

    protected static <T> Result createWithForeignEntities(Class<T> clazz, Callback<T> callback) {
        Logger.debug("Creating a " + clazz.getSimpleName() + " with foreign entities");

        Map<String, String> data = form().bindFromRequest().data();
        Map<String, String> validatedFormData = new HashMap<>();

        for (Field field : clazz.getFields()) {
            String fieldName = field.getName();

            if (field.isAnnotationPresent(ForeignEntity.class)) {
                if (data.containsKey(fieldName)) {
                    long id = checkId(data.get(fieldName));
                    if (INVALID_ID == id) {
                        return badRequestJson(fieldName + " must be a positive long");
                    }

                    Optional<?> entityOptional = DbUtils.findEntityById(field.getType(), id);
                    if (entityOptional.isPresent()) {
                        JsonNode jsonNode = toJson(entityOptional.get());
                        formatFormData(fieldName, jsonNode, validatedFormData);
                    } else {
                        return DbUtils.getNotFoundResult(field.getType().getSimpleName(), id);
                    }
                } else if (field.isAnnotationPresent(Constraints.Required.class)) {
                    return badRequestJson(clazz.getSimpleName() + "." + fieldName + " is required!");
                }
            } else if (data.containsKey(fieldName)) {
                validatedFormData.put(fieldName, data.get(fieldName));
            }
        }

        Form<T> form = Form.form(clazz).bind(validatedFormData);

        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        } else {
            T entity = form.get();
            JPA.em().persist(entity);

            Optional.ofNullable(callback).ifPresent(cb -> cb.success(entity));

            return created(toJson(entity));
        }
    }

    private static void formatFormData(String nodeName, JsonNode node, Map<String, String> data) {
        node.fields().forEachRemaining(jsonField -> {
            if (jsonField.getValue().isValueNode()) {
                data.put(nodeName + "." + jsonField.getKey(), jsonField.getValue().toString().replace("\"", ""));
            } else {
                formatFormData(nodeName + "." + jsonField.getKey(), jsonField.getValue(), data);
            }
        });
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
            return DbUtils.getNotFoundResult(clazz, id);
        }
    }

    protected static <T> Result delete(Class<T> clazz, long id) {
        Logger.debug("Deleting " + clazz.getSimpleName() + " with id " + id);

        boolean deleted = DbUtils.deleteEntityById(clazz, id);
        if (deleted) {
            return OK_RESULT;
        } else {
            return DbUtils.getNotFoundResult(clazz, id);
        }
    }

    protected static <T> Result show(Class<T> clazz, long id) {
        Logger.debug("Showing " + clazz.getSimpleName() + " with id " + id);

        Optional<T> entityOptional = DbUtils.findEntityById(clazz, id);
        if (entityOptional.isPresent()) {
            return okJson(entityOptional.get());
        } else {
            return DbUtils.getNotFoundResult(clazz, id);
        }
    }

    public static Result okJson(Object obj) {
        return ok(toJson(obj));
    }

    public static Result badRequestJson(Object obj) {
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
        all.get("publicRooms").forEach(room -> JPA.em().persist(room));
        all.get("messages").forEach(message -> JPA.em().persist(message));
        all.get("privateRooms").forEach(privateRoom -> JPA.em().persist(privateRoom));
        all.get("requests").forEach(request -> JPA.em().persist(request));
        return OK_RESULT;
    }
}
