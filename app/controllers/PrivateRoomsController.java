package controllers;

import models.entities.PrivateRoom;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Result;

public class PrivateRoomsController extends BaseController {

    @Transactional
    public static Result createRoom() {
        return createWithForeignEntities(PrivateRoom.class, createdRoom -> {
            createdRoom.senderId = createdRoom.sender.userId;
            createdRoom.receiverId = createdRoom.receiver.userId;
        });
    }

    @Transactional
    public static Result getRooms() {
        return read(PrivateRoom.class);
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
        String result = PrivateRoom.removeUser(roomId, userId);
        if (OK_STRING.equals(result)) {
            return OK_RESULT;
        } else {
            return badRequestJson(result);
        }
    }
}
