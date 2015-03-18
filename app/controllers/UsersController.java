package controllers;

import models.entities.User;
import play.db.jpa.Transactional;
import play.mvc.Result;
import utils.DbUtils;

import java.util.Optional;

import static play.data.Form.form;

public class UsersController extends BaseController {

    @Transactional
    public static Result createUser() {
        return create(User.class);
    }

    @Transactional
    public static Result getUsers() { return read(User.class); }

    @Transactional
    public static Result showUser(long id) {
        return show(User.class, id);
    }

    @Transactional
    public static Result updateUser(long id) {
        return update(User.class, id);
    }

    @Transactional
    public static Result deleteUser(long id) {
        return delete(User.class, id);
    }

    @Transactional
    public static Result sendNotification(long userId) {
        Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);

        if (userOptional.isPresent()) {
            String result = userOptional.get().sendNotification(form().bindFromRequest().data());
            if (OK_STRING.equals(result)) {
                return OK_RESULT;
            } else {
                return badRequestJson(result);
            }
        } else {
            return DbUtils.getNotFoundResult(User.ENTITY_NAME, userId);
        }
    }
}
