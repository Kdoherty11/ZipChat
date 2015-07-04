package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import models.Platform;
import models.entities.Device;
import models.entities.User;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;
import services.DeviceService;
import services.UserService;
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

    private final UserService userService;
    private final DeviceService deviceService;

    @Inject
    public DevicesController(final DeviceService deviceService, final UserService userService) {
        this.deviceService = deviceService;
        this.userService = userService;
    }

    @Transactional
    public Result createDevice() {
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

        Optional<User> userOptional = userService.findById(userId);

        if (userOptional.isPresent()) {
            Device device = new Device(userOptional.get(), regId, Platform.valueOf(platform));
            deviceService.save(device);
            ObjectNode jsonResponse = Json.newObject().put("deviceId", device.deviceId);
            return created(jsonResponse);
        } else {
            return entityNotFound(User.class, userId);
        }
    }

    @Transactional
    public Result updateDeviceInfo(long deviceId, String regId) {
        Optional<Device> deviceOptional = deviceService.findById(deviceId);

        if (deviceOptional.isPresent()) {
            Device device = deviceOptional.get();

            long userId = device.user.userId;
            if (isUnauthorized(userId)) {
                return forbidden();
            }

            if (!device.regId.equals(regId)) {
                device.regId = regId;
            }
            return OK_RESULT;
        } else {
            return entityNotFound(Device.class, deviceId);
        }
    }

}
