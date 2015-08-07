
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kdoherty/dev/ideaProjects/ZipChat/conf/routes
// @DATE:Fri Aug 07 17:18:08 EDT 2015

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseRequestsController RequestsController = new controllers.ReverseRequestsController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseRoomSocketsController RoomSocketsController = new controllers.ReverseRoomSocketsController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseDevicesController DevicesController = new controllers.ReverseDevicesController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseUsersController UsersController = new controllers.ReverseUsersController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReversePublicRoomsController PublicRoomsController = new controllers.ReversePublicRoomsController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseStatusController StatusController = new controllers.ReverseStatusController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReversePrivateRoomsController PrivateRoomsController = new controllers.ReversePrivateRoomsController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseMessagesController MessagesController = new controllers.ReverseMessagesController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseRequestsController RequestsController = new controllers.javascript.ReverseRequestsController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseRoomSocketsController RoomSocketsController = new controllers.javascript.ReverseRoomSocketsController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseDevicesController DevicesController = new controllers.javascript.ReverseDevicesController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseUsersController UsersController = new controllers.javascript.ReverseUsersController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReversePublicRoomsController PublicRoomsController = new controllers.javascript.ReversePublicRoomsController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseStatusController StatusController = new controllers.javascript.ReverseStatusController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReversePrivateRoomsController PrivateRoomsController = new controllers.javascript.ReversePrivateRoomsController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseMessagesController MessagesController = new controllers.javascript.ReverseMessagesController(RoutesPrefix.byNamePrefix());
  }

}
