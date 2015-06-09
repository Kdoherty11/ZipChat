package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.entities.User;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import security.SecurityHelper;
import validation.DataValidator;
import validation.FieldValidator;
import validation.validators.Validators;

import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;

public class UsersController extends BaseController {

    @Transactional
    public static Result createUser() {
        Map<String, String> data = form().bindFromRequest().data();
        String fbAccessTokenKey = "fbAccessToken";
        String fbAccessToken = data.get(fbAccessTokenKey);

        DataValidator validator = new DataValidator(
                new FieldValidator<>(fbAccessTokenKey, fbAccessToken, Validators.required())
        );

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        JsonNode facebookUserJson = User.getFacebookInformation(fbAccessToken);
        if (facebookUserJson.has("error")) {
            Logger.error("Invalid facebook access token received in user creation: "
                    + facebookUserJson.get("error").get("message").asText());
            return badRequest(facebookUserJson);
        }

        String facebookId = facebookUserJson.get("id").asText();
        String name = facebookUserJson.get("name").asText();
        String gender = facebookUserJson.get("gender").asText();

        User user = new User();
        user.facebookId = facebookId;
        user.name = name;
        user.gender = gender;

        Optional<User> existingUserOptional = User.byFacebookId(facebookId);
        if (existingUserOptional.isPresent()) {
            User existing = existingUserOptional.get();
            user.userId = existing.userId;

            JPA.em().merge(user);
        } else {
            JPA.em().persist(user);
        }

        ObjectNode response = Json.newObject();
        response.put("userId", user.userId);
        response.put("facebookId", facebookId);
        response.put("name", name);
        response.put("authToken", SecurityHelper.generateAuthToken(user.userId));

        if (existingUserOptional.isPresent()) {
            return ok(response);
        } else {
            return created(response);
        }
    }

    @Transactional
    public static Result auth(String fbAccessToken) {
        JsonNode facebookUserJson = User.getFacebookInformation(fbAccessToken);
        if (facebookUserJson.has("error")) {
            Logger.error("Invalid facebook access token received in auth: "
                    + facebookUserJson.get("error").get("message").asText());
            return badRequest(facebookUserJson);
        }

        String facebookId = facebookUserJson.get("id").asText();
        Optional<User> existingUserOptional = User.byFacebookId(facebookId);

        if (existingUserOptional.isPresent()) {
            ObjectNode response = Json.newObject();
            response.put("authToken", SecurityHelper.generateAuthToken(existingUserOptional.get().userId));
            return ok(response);
        } else {
            return badRequest(Json.toJson("facebook access token doesn't match any users"));
        }
    }
}
