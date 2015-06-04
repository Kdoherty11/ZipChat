package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.primitives.Longs;
import models.entities.User;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import security.SecurityHelper;
import utils.DbUtils;
import validation.DataValidator;
import validation.FieldValidator;
import validation.Validators;

import java.util.Optional;

import static play.data.Form.form;

public class UsersController extends Controller {

    @Transactional
    public static Result createUser() {
        Form<User> form = Form.form(User.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        } else {
            User user = form.get();
            JPA.em().persist(user);

            ObjectNode response = Json.newObject();
            response.put("userId", user.userId);
            response.put("token", SecurityHelper.generateAuthToken(user.userId));
            return created(response);
        }
    }

    public static Result login() {
        String userIdKey = "userId";
        Long userId = Longs.tryParse(form().bindFromRequest().get(userIdKey));

        DataValidator validator = new DataValidator(new FieldValidator<>(userIdKey, userId,
                Validators.required(),
                Validators.positive()));
        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        ObjectNode response = Json.newObject();
        response.put("token", SecurityHelper.generateAuthToken(userId));

        return ok(response);
    }

    @Transactional
    public static Result getUsers() { return BaseController.read(User.class); }

    @Transactional
    public static Result showUser(long id) {
        return BaseController.show(User.class, id);
    }

    @Transactional
    public static Result updateUser(long id) {
        return BaseController.update(User.class, id);
    }

    @Transactional
    public static Result deleteUser(long id) {
        return BaseController.delete(User.class, id);
    }

    @Transactional
    public static Result sendNotification(long userId) {
        Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);

        if (userOptional.isPresent()) {
            String result = userOptional.get().sendNotification(form().bindFromRequest().data());
            if (BaseController.OK_STRING.equals(result)) {
                return BaseController.OK_RESULT;
            } else {
                return BaseController.badRequestJson(result);
            }
        } else {
            return DbUtils.getNotFoundResult(User.ENTITY_NAME, userId);
        }
    }
}
