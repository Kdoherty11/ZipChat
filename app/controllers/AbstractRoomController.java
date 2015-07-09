package controllers;

import play.mvc.Result;
import services.MessageService;
import validation.DataValidator;
import validation.FieldValidator;
import validation.validators.Validators;

/**
 * Created by kdoherty on 7/8/15.
 */
public abstract class AbstractRoomController extends BaseController {

    private final MessageService messageService;

    public AbstractRoomController(final MessageService messageService) {
        this.messageService = messageService;
    }

    protected Result getMessagesHelper(long roomId, int limit, int offset) {
        DataValidator validator = new DataValidator(
                new FieldValidator<>("limit", limit, Validators.min(0)),
                new FieldValidator<>("offset", offset, Validators.min(0)));

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        return okJson(messageService.getMessages(roomId, limit, offset));
    }

}
