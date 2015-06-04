package controllers;

import com.google.common.primitives.Longs;
import models.entities.Request;
import play.db.jpa.Transactional;
import play.mvc.Result;
import utils.DbUtils;
import utils.NotificationUtils;
import validation.DataValidator;
import validation.FieldValidator;
import validation.Validators;

import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;


public class RequestsController extends BaseController {

    @Transactional
    public static Result createRequest() {
        long senderId = Longs.tryParse(form().bindFromRequest().data().get("sender"));
        if (isUnauthorized(senderId)) {
            return forbidden();
        }
        return createWithForeignEntities(Request.class, createdRequest ->
                NotificationUtils.sendChatRequest(createdRequest.sender, createdRequest.receiver));
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
            return DbUtils.getNotFoundResult(Request.ENTITY_NAME, requestId);
        }
    }

    @Transactional
    public static Result getStatus(long senderId, long receiverId) {
        return ok(Request.getStatus(senderId, receiverId));
    }
}
