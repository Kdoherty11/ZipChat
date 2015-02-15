package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.entities.User;
import play.mvc.Controller;
import play.mvc.Result;

import static play.data.Form.form;
import static play.libs.F.Promise;


public class UsersController extends Controller {

    private static final CrudUtils.Callback DEFAULT_CB = new CrudUtils.Callback() {
        @Override
        public Result success(JsonNode entity) {
            return ok(entity);
        }

        @Override
        public Result failure(JsonNode error) {
            return badRequest(error);
        }
    };

    public static Promise<Result> createUser() {
        return CrudUtils.create(form(User.class).bindFromRequest(), DEFAULT_CB);

    }

    public static Promise<Result> getUsers() {
        return CrudUtils.read(User.class, entities -> ok(entities));

    }

    public static Promise<Result> getUser(String id) {
        return CrudUtils.show(id, User.class, DEFAULT_CB);
    }

    public static Promise<Result> updateUser(String id) {
        return CrudUtils.update(id, User.class, form().bindFromRequest(), DEFAULT_CB);
    }

    public static Promise<Result> deleteUser(String id) {
        return CrudUtils.delete(id, User.class, DEFAULT_CB);
    }

}
