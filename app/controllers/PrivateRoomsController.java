package controllers;

import models.entities.PrivateRoom;
import play.db.jpa.Transactional;
import play.mvc.Result;

public class PrivateRoomsController extends BaseController {

    @Transactional
    public static Result createRoom() {
        return create(PrivateRoom.class);
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
}