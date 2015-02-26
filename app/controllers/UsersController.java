package controllers;

import models.entities.User;
import play.db.jpa.Transactional;
import play.mvc.Result;

import java.util.Map;

import static play.data.Form.form;
import static play.libs.F.Promise;

public class UsersController extends BaseController {

    @Transactional
    public static Result createUser() {
        return create(User.class);
    }

    @Transactional
    public static Result getUsers() {
        return read(User.class);
    }

    @Transactional
    public static Result showUser(String id) {
        return show(User.class, id);
    }

    @Transactional
    public static Result updateUser(String id) {
        return update(User.class, id);
    }

    @Transactional
    public static Result deleteUser(String id) {
        return delete(User.class, id);
    }

    @Transactional
    public static Promise<Result> sendNotification(String userId) {
        Map<String, String> data = form().bindFromRequest().data();
        return User.sendNotification(userId, data).map(response -> ok(response));
    }
}
