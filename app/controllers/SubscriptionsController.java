package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import utils.CrudUtils;
import models.entities.Subscription;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

import static play.data.Form.form;

public class SubscriptionsController extends Controller {

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

    public static F.Promise<Result> createRequest() {
        return CrudUtils.create(form(Subscription.class).bindFromRequest(), DEFAULT_CB);
    }

}
