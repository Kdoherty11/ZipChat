package controllers;

import models.entities.PrivateRoom;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Result;
import utils.DbUtils;

import java.util.Optional;

public class PrivateRoomsController extends BaseController {

    @Transactional
    public static Result createRoom() {
        return createWithForeignEntities(PrivateRoom.class);
    }

    @Transactional
    public static Result updateRoom(long id) {
        return update(PrivateRoom.class, id);
    }

    @Transactional
    public static Result showRoom(long id) {
        return show(PrivateRoom.class, id);
    }

    @Transactional
    public static Result deleteRoom(long id) {
        return delete(PrivateRoom.class, id);
    }

    @Transactional
    public static Result getRoomsByUserId(long userId) {
        Logger.debug("Getting Private Rooms by userId: " + userId);
        return okJson(PrivateRoom.getRoomsByUserId(userId));
    }

    @Transactional
    public static Result leaveRoom(long roomId, long userId) {
        Optional<PrivateRoom> roomOptional = DbUtils.findEntityById(PrivateRoom.class, roomId);

        if (roomOptional.isPresent()) {
            PrivateRoom room = roomOptional.get();
            boolean removed = room.removeUser(userId);

            if (removed) {
                return OK_RESULT;
            } else {
                return badRequestJson("Unable to remove user with ID " + userId + " from the room because they are not in it");
            }

        } else {
            return DbUtils.getNotFoundResult(PrivateRoom.ENTITY_NAME, roomId);
        }
    }
}
