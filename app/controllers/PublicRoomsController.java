package controllers;

import com.google.common.primitives.Longs;
import models.entities.AbstractRoom;
import models.entities.PublicRoom;
import models.entities.User;
import play.db.jpa.Transactional;
import play.mvc.Result;
import utils.DbUtils;
import validation.DataValidator;
import validation.FieldValidator;
import validation.Validators;

import java.util.Map;
import java.util.Optional;

import static play.data.Form.form;

public class PublicRoomsController extends BaseController {

    @Transactional
    public static Result createRoom() {
        return create(PublicRoom.class);
    }

    @Transactional
    public static Result getRooms() {
        return read(PublicRoom.class);
    }

    @Transactional
    public static Result updateRoom(long id) {
        return update(PublicRoom.class, id);
    }

    @Transactional
    public static Result showRoom(long id) {
        return show(PublicRoom.class, id);
    }

    @Transactional
    public static Result deleteRoom(long id) {
        return delete(PublicRoom.class, id);
    }

    @Transactional
    public static Result getGeoRooms(double lat, double lon) {
        return okJson(PublicRoom.allInGeoRange(lat, lon));
    }

    @Transactional
    public static Result createSubscription(long roomId) {
        Map<String, String> data = form().bindFromRequest().data();

        String userIdKey = "userId";

        Long userId = Longs.tryParse(data.get(userIdKey));

        if (userId == null) {
            return FieldValidator.typeError(userIdKey, Long.class);
        }

        DataValidator validator = new DataValidator(
                new FieldValidator<>(userIdKey, userId, Validators.required(), Validators.positive()));

        if (validator.hasErrors()) {
            return badRequest(validator.errorsAsJson());
        }

        Optional<PublicRoom> roomOptional = DbUtils.findEntityById(PublicRoom.class, roomId);
        if (roomOptional.isPresent()) {

            Optional<User> userOptional = DbUtils.findEntityById(User.class, userId);
            if (userOptional.isPresent()) {
                roomOptional.get().addSubscription(userOptional.get());
                return OK_RESULT;
            } else {
                return DbUtils.getNotFoundResult(User.ENTITY_NAME, userId);
            }
        } else {
            return DbUtils.getNotFoundResult(PublicRoom.ENTITY_NAME, roomId);
        }
    }

    @Transactional
    public static Result getSubscriptions(long roomId) {
        Optional<PublicRoom> roomOptional = DbUtils.findEntityById(PublicRoom.class, roomId);
        if (roomOptional.isPresent()) {
            return okJson(roomOptional.get().subscribers);
        } else {
            return DbUtils.getNotFoundResult(PublicRoom.ENTITY_NAME, roomId);
        }
    }

    @Transactional
    public static Result removeSubscription(long roomId, long userId) {
        Optional<PublicRoom> roomOptional = DbUtils.findEntityById(PublicRoom.class, roomId);
        if (roomOptional.isPresent()) {
            roomOptional.get().removeSubscription(userId);
            return OK_RESULT;
        } else {
            return DbUtils.getNotFoundResult(PublicRoom.ENTITY_NAME, roomId);
        }
    }

    @Transactional
    public static Result notifySubscribers(long roomId) {
        Optional<PublicRoom> roomOptional = DbUtils.findEntityById(PublicRoom.class, roomId);
        if (roomOptional.isPresent()) {
            roomOptional.get().notifySubscribers(form().bindFromRequest().data());
            return OK_RESULT;
        } else {
            return DbUtils.getNotFoundResult(PublicRoom.ENTITY_NAME, roomId);
        }
    }
}