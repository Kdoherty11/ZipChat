package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import models.Platform;
import models.entities.User;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;
import security.SecurityHelper;
import utils.DbUtils;
import validation.DataValidator;
import validation.FieldValidator;
import validation.validators.Validators;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;

public class UsersController extends BaseController {

    @Transactional
    public static Result createUser() {
        Map<String, String> data = form().bindFromRequest().data();
        String fbAccessTokenKey = "fbAccessToken";
        String platformKey = "platform";
        String regIdKey = "regId";

        String fbAccessToken = data.get(fbAccessTokenKey);
        String platform = data.get(platformKey);
        String regId = data.get(regIdKey);

        DataValidator validator = new DataValidator(
                new FieldValidator<>(fbAccessTokenKey, fbAccessToken, Validators.required()),
                new FieldValidator<>(fbAccessTokenKey, platform, Validators.required(), Validators.enumValue(Platform.class))
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
        user.platform = Platform.valueOf(platform);

        Optional<User> existingUserOptional = User.byFacebookId(facebookId);
        if (existingUserOptional.isPresent()) {
            User existing = existingUserOptional.get();
            user.userId = existing.userId;

            if (user.platform == existing.platform) {
                if (Strings.isNullOrEmpty(regId) && !existing.registrationIds.contains(regId)) {
                    user.registrationIds = existing.registrationIds;
                    user.registrationIds.add(regId);
                }
            } else {
                user.registrationIds = Collections.singletonList(regId);
            }

            JPA.em().merge(user);
        } else {
            user.registrationIds = Collections.singletonList(regId);
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

    @Transactional
    public static Result getUsers() { return BaseController.read(User.class); }

    @Transactional
    public static Result showUser(long id) {
        return BaseController.show(User.class, id);
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public static Result updateUser(long userId) {
        if (BaseController.isUnauthorized(userId)) {
            return forbidden();
        }
        return BaseController.update(User.class, userId);
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
