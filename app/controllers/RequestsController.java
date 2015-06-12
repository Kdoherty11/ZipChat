package controllers;

import com.google.common.primitives.Longs;
import models.entities.AbstractUser;
import models.entities.Request;
import models.entities.User;
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

@Security.Authenticated(Secured.class)
public class RequestsController extends BaseController {

    @Transactional
    public static Result createRequest() {
        Map<String, String> data = form().bindFromRequest().data();

        String senderKey = "sender";
        String receiverKey = "receiver";

        Long senderId = Longs.tryParse(data.get(senderKey));
        Long receiverId = Longs.tryParse(data.get(receiverKey));

        if (senderId == null) {
            return badRequestJson(FieldValidator.typeError(senderKey, Long.class));
        }

        if (receiverId == null) {
            return badRequestJson(FieldValidator.typeError(receiverKey, Long.class));
        }

        if (isUnauthorized(senderId)) {
            return forbidden();
        }

        Optional<User> senderOptional = DbUtils.findEntityById(User.class, senderId);
        if (senderOptional.isPresent()) {
            Optional<AbstractUser> receiverOptional = DbUtils.findEntityById(AbstractUser.class, receiverId);
            if (receiverOptional.isPresent()) {
                senderOptional.get().sendChatRequest(receiverOptional.get());
                return OK_RESULT;
            } else {
                return DbUtils.getNotFoundResult(AbstractUser.class, receiverId);
            }
        } else {
            return DbUtils.getNotFoundResult(User.class, senderId);
        }
    }

    @Transactional
    public static Result getRequestsByReceiver(long receiverId) {
        if (isUnauthorized(receiverId)) {
            return forbidden();
        }
        return okJson(Request.getPendingRequestsByReceiver(receiverId));
    }

    @Transactional
    public static Result handleResponse(long requestId) {
        String responseKey = "status";

        Map<String, String> formData = form().bindFromRequest(responseKey).data();

        DataValidator validator = new DataValidator(
                new FieldValidator<>(responseKey, formData.get(responseKey), Validators.required(),
                        Validators.whiteList(Request.Status.accepted.name(), Request.Status.denied.name()))
        );

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        Optional<Request> requestOptional = DbUtils.findEntityById(Request.class, requestId);
        if (requestOptional.isPresent()) {

            Request request = requestOptional.get();
            if (isUnauthorized(request.receiver.userId)) {
                return forbidden();
            }

            request.handleResponse(Request.Status.valueOf(formData.get(responseKey)));

            return OK_RESULT;
        } else {
            return DbUtils.getNotFoundResult(Request.class, requestId);
        }
    }

    @Transactional
    public static Result getStatus(long senderId, long receiverId) {
        return ok(Request.getStatus(senderId, receiverId));
    }
}
