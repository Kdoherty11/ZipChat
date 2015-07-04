package controllers;

import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import models.entities.AbstractUser;
import models.entities.Request;
import models.entities.User;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;
import services.AbstractUserService;
import services.RequestService;
import services.UserService;
import validation.DataValidator;
import validation.FieldValidator;
import validation.validators.Validators;

import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;

@Security.Authenticated(Secured.class)
public class RequestsController extends BaseController {

    private RequestService requestService;
    private UserService userService;
    private AbstractUserService abstractUserService;

    @Inject
    public RequestsController(final RequestService requestService,
                              final AbstractUserService abstractUserService,
                              final UserService userService) {
        this.requestService = requestService;
        this.abstractUserService = abstractUserService;
        this.userService = userService;
    }

    @Transactional
    public Result createRequest() {
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

        Optional<User> senderOptional = userService.findById(senderId);
        if (senderOptional.isPresent()) {
            Optional<AbstractUser> receiverOptional = abstractUserService.findById(receiverId);
            if (receiverOptional.isPresent()) {
                userService.sendChatRequest(senderOptional.get(), receiverOptional.get());
                return OK_RESULT;
            } else {
                return entityNotFound(AbstractUser.class, receiverId);
            }
        } else {
            return entityNotFound(User.class, senderId);
        }
    }

    @Transactional(readOnly = true)
    public Result getRequestsByReceiver(long receiverId) {
        if (isUnauthorized(receiverId)) {
            return forbidden();
        }
        return okJson(requestService.findPendingRequestsByReceiver(receiverId));
    }

    @Transactional
    public Result handleResponse(long requestId) {
        String responseKey = "status";

        Map<String, String> formData = form().bindFromRequest(responseKey).data();

        DataValidator validator = new DataValidator(
                new FieldValidator<>(responseKey, formData.get(responseKey), Validators.required(),
                        Validators.whiteList(Request.Status.accepted.name(), Request.Status.denied.name()))
        );

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        Optional<Request> requestOptional = requestService.findById(requestId);
        if (requestOptional.isPresent()) {

            Request request = requestOptional.get();
            if (isUnauthorized(request.receiver.userId)) {
                return forbidden();
            }

            if (request.status != Request.Status.pending) {
                return badRequestJson("This request has already been responded to");
            }

            requestService.handleResponse(request, Request.Status.valueOf(formData.get(responseKey)));

            return OK_RESULT;
        } else {
            return entityNotFound(Request.class, requestId);
        }
    }

    @Transactional(readOnly = true)
    public Result getStatus(long senderId, long receiverId) {
        return ok(requestService.getStatus(senderId, receiverId));
    }
}
