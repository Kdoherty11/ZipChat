package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.primitives.Longs;
import models.Platform;
import models.entities.Device;
import models.entities.User;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;
import utils.DbUtils;
import validation.DataValidator;
import validation.FieldValidator;
import validation.validators.Validators;

import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;

/**
 * Created by kevin on 6/7/15.
 */
@Security.Authenticated(Secured.class)
public class DevicesController extends BaseController {

    @Transactional
    public static Result createDevice() {
        Map<String, String> data = form().bindFromRequest().data();
        String userIdKey = "userId";

        Long userId = Longs.tryParse(data.get(userIdKey));
        if (userId == null) {
            return FieldValidator.typeError(userIdKey, Long.class);
        }

        if (isUnauthorized(userId)) {
            return forbidden();
        }

        String regIdKey = "regId";
        String platformKey = "platform";

        String regId = data.get(regIdKey);
        String platform = data.get(platformKey);

        DataValidator validator = new DataValidator(
                new FieldValidator<>(regIdKey, regId, Validators.required()),
                new FieldValidator<>(platformKey, platform, Validators.required(), Validators.enumValue(Platform.class))
        );

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);

        if (userOptional.isPresent()) {
            Device device = new Device(userOptional.get(), regId, Platform.valueOf(platform));
            JPA.em().persist(device);
            ObjectNode jsonResponse = Json.newObject().put("deviceId", device.deviceId);
            return ok(jsonResponse);
        } else {
            return DbUtils.getNotFoundResult(User.class, userId);
        }
    }

    @Transactional
    public static Result updateDeviceInfo(long notificationInfoId, String regId) {
        Optional<Device> infoOptional = DbUtils.findEntityById(Device.class, notificationInfoId);

        if (infoOptional.isPresent()) {
            Device info = infoOptional.get();

            long userId = info.user.userId;
            if (isUnauthorized(userId)) {
                return forbidden();
            }

            if (!info.regId.equals(regId)) {
                info.regId = regId;
            }
            return OK_RESULT;
        } else {
            return DbUtils.getNotFoundResult(Device.class, notificationInfoId);
        }
    }

}
