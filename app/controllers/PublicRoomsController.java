package controllers;

import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.PublicRoom;
import models.User;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;
import services.MessageService;
import services.PublicRoomService;
import services.SecurityService;
import services.UserService;
import validation.DataValidator;
import validation.FieldValidator;
import validation.validators.Validators;

import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;
import static play.libs.Json.toJson;

@Singleton
@Security.Authenticated(Secured.class)
public class PublicRoomsController extends AbstractRoomController {

    private final PublicRoomService publicRoomService;
    private final SecurityService securityService;
    private final UserService userService;

    @Inject
    public PublicRoomsController(final PublicRoomService publicRoomService, final MessageService messageService,
                                 final SecurityService securityService, final UserService userService) {
        super(messageService);
        this.publicRoomService = publicRoomService;
        this.securityService = securityService;
        this.userService = userService;
    }

    @Transactional
    public Result createRoom() {
        Form<PublicRoom> form = Form.form(PublicRoom.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        } else {
            String creatorIdKey = "creatorId";
            Map<String, String> formData = Form.form().bindFromRequest(creatorIdKey).data();
            String creatorIdStr = formData.get(creatorIdKey);

            DataValidator dataValidator = new DataValidator(
                    new FieldValidator<>(creatorIdKey, creatorIdStr,
                            Validators.required(), Validators.stringToLong()));

            if (dataValidator.hasErrors()) {
                return badRequest(dataValidator.errorsAsJson());
            }

            long creatorId = Longs.tryParse(creatorIdStr);

            Optional<User> creatorOptional = userService.findById(creatorId);
            if (creatorOptional.isPresent()) {
                PublicRoom room = form.get();
                room.creator = creatorOptional.get();
                publicRoomService.save(room);
                return created(toJson(room));
            } else {
                return entityNotFound(User.class, creatorId);
            }
        }
    }

    @Transactional(readOnly = true)
    public Result getGeoRooms(double lat, double lon) {
        return okJson(publicRoomService.allInGeoRange(lat, lon));
    }

    @Transactional
    public Result createSubscription(long roomId) {
        Map<String, String> data = form().bindFromRequest().data();

        String userIdKey = "userId";

        if (!data.containsKey(userIdKey)) {
            return badRequestJson(userIdKey + " is required");
        }

        Long userId = Longs.tryParse(data.get(userIdKey));

        if (userId == null) {
            return FieldValidator.typeError(userIdKey, Long.class);
        }

        return publicRoomUserActionHelper(roomId, userId, new PublicRoomUserAction() {
            @Override
            public boolean publicRoomAction(PublicRoom publicRoom, User user) {
                return publicRoomService.subscribe(publicRoom, user);
            }

            @Override
            public Result onActionSuccess() {
                return created();
            }

            @Override
            public Result onActionFailed(User user) {
                return badRequest("User " + user.userId + " is already subscribed");
            }
        });
    }

    @Transactional
    public Result removeSubscription(long roomId, long userId) {
        return publicRoomUserActionHelper(roomId, userId, new PublicRoomUserAction() {
            @Override
            public boolean publicRoomAction(PublicRoom publicRoom, User user) {
                return publicRoomService.unsubscribe(publicRoom, user);
            }

            @Override
            public Result onActionSuccess() {
                return OK_RESULT;
            }

            @Override
            public Result onActionFailed(User user) {
                return notFound("User " + user.userId + " is not subscribed to the room");
            }
        });
    }

    // Public due to http://stackoverflow.com/a/21442580/3258892
    public interface PublicRoomUserAction {
        boolean publicRoomAction(PublicRoom publicRoom, User user);

        Result onActionSuccess();

        Result onActionFailed(User user);
    }

    private Result publicRoomUserActionHelper(long roomId, long userId, PublicRoomUserAction cb) {
        if (securityService.isUnauthorized(userId)) {
            return forbidden();
        }

        DataValidator validator = new DataValidator(
                new FieldValidator<>("roomId", roomId, Validators.positive()),
                new FieldValidator<>("userId", userId, Validators.positive()));

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        Optional<PublicRoom> roomOptional = publicRoomService.findById(roomId);
        if (roomOptional.isPresent()) {
            Optional<User> userOptional = userService.findById(userId);

            if (userOptional.isPresent()) {
                boolean result = cb.publicRoomAction(roomOptional.get(), userOptional.get());

                if (!result) {
                    return cb.onActionFailed(userOptional.get());
                }

                return cb.onActionSuccess();
            } else {
                return entityNotFound(User.class, roomId);
            }

        } else {
            return entityNotFound(PublicRoom.class, roomId);
        }
    }

    @Transactional(readOnly = true)
    public Result getMessages(long roomId, int limit, int offset) {
        Optional<PublicRoom> publicRoomOptional = publicRoomService.findById(roomId);
        if (!publicRoomOptional.isPresent()) {
            return entityNotFound(PublicRoom.class, roomId);
        }

        return getMessagesHelper(roomId, limit, offset);
    }

//    Used for testing
//    ---------------
//    @Transactional(readOnly = true)
//    public Result getRooms() {
//        return read(PublicRoom.class);
//    }
}