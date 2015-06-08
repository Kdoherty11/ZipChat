package controllers;

import com.google.common.primitives.Longs;
import models.Platform;
import models.entities.NotificationInfo;
import models.entities.User;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
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
public class NotificationInfoController extends BaseController {

    @Transactional
    @Security.Authenticated(Secured.class)
    public static Result createNotificationInfo() {
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
            NotificationInfo notificationInfo = new NotificationInfo(userOptional.get(), regId, Platform.valueOf(platform));
            //notificationInfo.addToUser();
            JPA.em().persist(notificationInfo);
            Logger.debug("Created notification info: " + notificationInfo);
            return okJson(notificationInfo);
        } else {
            return DbUtils.getNotFoundResult(User.class, userId);
        }
    }

    @Transactional
    @Security.Authenticated(Secured.class)
    public static Result updateNotificationInfo(long notificationInfoId, String regId) {
        Optional<NotificationInfo> infoOptional = DbUtils.findEntityById(NotificationInfo.class, notificationInfoId);

        if (infoOptional.isPresent()) {
            NotificationInfo info = infoOptional.get();

            long userId = info.user.userId;
            if (isUnauthorized(userId)) {
                return forbidden();
            }

            if (!info.regId.equals(regId)) {
                info.regId = regId;
            }
            return OK_RESULT;
        } else {
            return DbUtils.getNotFoundResult(NotificationInfo.class, notificationInfoId);
        }
    }

}
