package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.entities.PublicRoom;
import models.entities.User;
import models.sockets.RoomSocket;
import play.Logger;
import play.Play;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.WebSocket;
import security.Secured;
import security.SecurityHelper;
import services.PublicRoomService;
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
public class PublicRoomsController extends BaseController {

    private final PublicRoomService publicRoomService;
    private final MessagesController messagesController;
    private final SecurityHelper securityHelper;
    private final UserService userService;

    @Inject
    public PublicRoomsController(final PublicRoomService publicRoomService, final MessagesController messagesController,
                                 final SecurityHelper securityHelper, final UserService userService) {
        this.publicRoomService = publicRoomService;
        this.messagesController = messagesController;
        this.securityHelper = securityHelper;
        this.userService = userService;
    }

    @Transactional
    public Result createRoom() {
        Form<PublicRoom> form = Form.form(PublicRoom.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        } else {
            PublicRoom room = form.get();
            publicRoomService.save(room);
            return created(toJson(room));
        }
    }

    @Transactional(readOnly = true)
    public Result getRooms() {
        return read(PublicRoom.class);
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

        DataValidator validator = new DataValidator(
                new FieldValidator<>(userIdKey, userId, Validators.positive()));

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
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

    private Result publicRoomUserActionHelper(long roomId, long userId, PublicRoomUserAction cb) {
        if (isUnauthorized(userId)) {
            return forbidden();
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

    // Public due to http://stackoverflow.com/a/21442580/3258892
    public interface PublicRoomUserAction {
        boolean publicRoomAction(PublicRoom publicRoom, User user);
        Result onActionSuccess();
        Result onActionFailed(User user);
    }

    @Transactional
    public WebSocket<JsonNode> joinRoom(final long roomId, final long userId, String authToken) {
        Optional<Long> userIdOptional = securityHelper.getUserId(authToken);
        if ((!userIdOptional.isPresent() || userIdOptional.get() != userId) && Play.isProd()) {
            return WebSocket.reject(forbidden());
        }

        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
                try {
                    RoomSocket.join(roomId, userId, in, out);
                } catch (Exception ex) {
                    Logger.error("Problem joining the RoomSocket: " + ex.getMessage());
                }
            }
        };
    }

    @Transactional(readOnly = true)
    public Result getMessages(long roomId, int limit, int offset) {
        Optional<PublicRoom> publicRoomOptional = publicRoomService.findById(roomId);
        if (!publicRoomOptional.isPresent()) {
            return entityNotFound(PublicRoom.class, roomId);
        }

        return messagesController.getMessages(roomId, limit, offset);
    }
}