package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import static play.data.Form.form;
import static play.libs.F.Promise;


public class UsersController extends Controller {

    public static Promise<Result> createUser() {
        Logger.debug("Received create user request");

        return CrudUtils.create(form(User.class).bindFromRequest(), new CrudUtils.Callback() {
            @Override
            public Result success(JsonNode entity) {
                return ok(entity);
            }

            @Override
            public Result failure(JsonNode error) {
                return badRequest(error);
            }
        });

    }

    public static Promise<Result> getUsers() {
        Logger.debug("Received get users request");

        return CrudUtils.read(User.class, entities -> ok(entities));

    }

    public static Promise<Result> getUser(String id) {
        Logger.debug("Received get user request for id: " + id);
        return CrudUtils.show(id, User.class, new CrudUtils.Callback() {
            @Override
            public Result success(JsonNode entity) {
                return ok(entity);
            }

            @Override
            public Result failure(JsonNode error) {
                return badRequest(error);
            }
        });
    }

    public static Promise<Result> updateUser(String id) {
        Logger.debug("Received update user request for id: " + id);

        return CrudUtils.update(
                id, User.class, form().bindFromRequest(), new CrudUtils.Callback() {
                    @Override
                    public Result success(JsonNode user) {
                        return ok(user);
                    }

                    @Override
                    public Result failure(JsonNode error) {
                        return badRequest(error);
                    }
                });
    }

    public static Promise<Result> deleteUser(String id) {
        Logger.debug("Received delete user request for id: " + id);

        return CrudUtils.delete(id, User.class, new CrudUtils.Callback() {
            @Override
            public Result success(JsonNode entity) {
                return ok(entity);
            }

            @Override
            public Result failure(JsonNode error) {
                return badRequest(error);
            }
        });
    }

}
