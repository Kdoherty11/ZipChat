package controllers;

import models.NoUpdate;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;
import play.Logger;
import play.data.Form;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Result;
import utils.DbUtils;

import javax.persistence.Id;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;
import static play.libs.Json.toJson;

public class BaseController extends Controller {

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

    protected static <T> Result read(Class<T> clazz) {
        Logger.debug("Getting all " + clazz.getSimpleName() + "s");

        CriteriaQuery<T> cq = JPA.em().getCriteriaBuilder().createQuery(clazz);

        Root<T> root = cq.from(clazz);
        CriteriaQuery<T> all = cq.select(root);

        TypedQuery<T> allQuery = JPA.em().createQuery(all);
        List<T> entities = allQuery.getResultList();

        return okJson(entities);
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
            return okJson("OK");
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

    private static String[] getUpdateWhiteList(Class clazz) {
        return Arrays.asList(clazz.getDeclaredFields())
                .stream()
                .filter(field -> canBeUpdated(field))
                .map(Field::getName)
                .toArray(String[]::new);
    }

    private static boolean canBeUpdated(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(Id.class) && !field.isAnnotationPresent(NoUpdate.class);
    }
}