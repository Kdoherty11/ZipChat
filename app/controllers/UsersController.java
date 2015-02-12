package controllers;

import models.ChatRoom;
import models.ChatRoomModel;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

import static play.data.Form.form;
import static play.libs.Json.toJson;

/**
 * Created by zacharywebert on 2/11/15.
 */
public class UsersController extends Controller {
    public static Result createUser() {
        Logger.debug("Received create user request");

        Form<User> userForm = form(User.class).bindFromRequest();
        if (userForm.hasErrors()) {
            return badRequest(userForm.errorsAsJson());
        } else {
            User user = userForm.get();
            user.save();
            return ok(toJson(user));
        }
    }

    public static Result getUsers() {
        Logger.debug("Received get users request");
        List<User> tasks = new Model.Finder(String.class, User.class).all();
        return ok(toJson(tasks));
    }

    public static Result getUser(String id) {
        Logger.debug("Received get user request for id: " + id);
        User user = new Model.Finder<>(String.class, User.class).byId(id);
        if (user == null) {
            return badRequest(toJson("User not found"));
        } else {
            return ok(toJson(user));
        }
    }

    public static Result updateUser(String id) {
        Logger.debug("Received update user request for id: " + id);
        User user = new Model.Finder<>(String.class, User.class).byId(id);
        DynamicForm dynamicForm = form().bindFromRequest();

        String name = dynamicForm.get("name");
        if (name != null) {
            user.name = name;
        }

        String facebookId = dynamicForm.get("facebookId");
        if (facebookId != null) {
            user.facebookId = facebookId;
        }

        String registrationId = dynamicForm.get("registrationId");
        if (registrationId != null) {
            user.registrationId = registrationId;
        }

        user.update();
        return ok(toJson(user));

    }

    public static Result deleteUser(String id) {
        Logger.debug("Received delete user request for id: " + id);
        User user = new Model.Finder<>(String.class, User.class).byId(id);
        if (user != null) {
            user.delete();
            return ok(toJson(user));
        } else {
            return badRequest("User not found");
        }
    }

}
