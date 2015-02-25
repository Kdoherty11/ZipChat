package controllers;

import play.mvc.Controller;

public class SubscriptionsController extends Controller {

//    @Transactional
//    public static Result createSubscription(long roomId) {
//        Optional<Room> roomOptional = CrudUtils.findEntityById(Room.class, roomId);
//
//        if (roomOptional.isPresent()) {
//
//            Map<String, String> formData = form().bindFromRequest().data();
//
//            if (!formData.containsKey("userId")) {
//                return badRequest(toJson("Field userId is required"));
//            }
//
//            long userId;
//            try {
//                userId = Long.valueOf(formData.get("userId"));
//            } catch (NumberFormatException e) {
//                return badRequest(toJson("userId must be a long"));
//            }
//            roomOptional.get().addSubscription(userId);
//            return ok(toJson("OK"));
//        } else {
//            return badRequest(toJson(CrudUtils.buildEntityNotFoundError("Room", roomId)));
//        }
//    }

}
