
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kdoherty/dev/ideaProjects/ZipChat/conf/routes
// @DATE:Fri Aug 07 17:18:08 EDT 2015

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset
import _root_.play.libs.F

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:7
  PublicRoomsController_0: controllers.PublicRoomsController,
  // @LINE:12
  RoomSocketsController_4: controllers.RoomSocketsController,
  // @LINE:14
  PrivateRoomsController_6: controllers.PrivateRoomsController,
  // @LINE:19
  MessagesController_5: controllers.MessagesController,
  // @LINE:24
  UsersController_2: controllers.UsersController,
  // @LINE:28
  DevicesController_3: controllers.DevicesController,
  // @LINE:31
  RequestsController_1: controllers.RequestsController,
  // @LINE:36
  StatusController_7: controllers.StatusController,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:7
    PublicRoomsController_0: controllers.PublicRoomsController,
    // @LINE:12
    RoomSocketsController_4: controllers.RoomSocketsController,
    // @LINE:14
    PrivateRoomsController_6: controllers.PrivateRoomsController,
    // @LINE:19
    MessagesController_5: controllers.MessagesController,
    // @LINE:24
    UsersController_2: controllers.UsersController,
    // @LINE:28
    DevicesController_3: controllers.DevicesController,
    // @LINE:31
    RequestsController_1: controllers.RequestsController,
    // @LINE:36
    StatusController_7: controllers.StatusController
  ) = this(errorHandler, PublicRoomsController_0, RoomSocketsController_4, PrivateRoomsController_6, MessagesController_5, UsersController_2, DevicesController_3, RequestsController_1, StatusController_7, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, PublicRoomsController_0, RoomSocketsController_4, PrivateRoomsController_6, MessagesController_5, UsersController_2, DevicesController_3, RequestsController_1, StatusController_7, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """publicRooms""", """controllers.PublicRoomsController.createRoom()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """publicRooms""", """controllers.PublicRoomsController.getGeoRooms(lat:Double, lon:Double)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """publicRooms/$roomId<[^/]+>/subscriptions""", """controllers.PublicRoomsController.createSubscription(roomId:Long)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """publicRooms/$roomId<[^/]+>/subscriptions/$userId<[^/]+>""", """controllers.PublicRoomsController.removeSubscription(roomId:Long, userId:Long)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """publicRooms/$roomId<[^/]+>/messages""", """controllers.PublicRoomsController.getMessages(roomId:Long, limit:Int, offset:Int)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """publicRooms/$roomId<[^/]+>/join""", """controllers.RoomSocketsController.joinPublicRoom(roomId:Long, userId:Long, authToken:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """privateRooms""", """controllers.PrivateRoomsController.getRoomsByUserId(userId:Long)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """privateRooms/$roomId<[^/]+>/leave""", """controllers.PrivateRoomsController.leaveRoom(roomId:Long, userId:Long)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """privateRooms/$roomId<[^/]+>/messages""", """controllers.PrivateRoomsController.getMessages(roomId:Long, limit:Int, offset:Int)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """privateRooms/$roomId<[^/]+>/join""", """controllers.RoomSocketsController.joinPrivateRoom(roomId:Long, userId:Long, authToken:String)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """messages/$messageId<[^/]+>/favorite""", """controllers.MessagesController.favorite(messageId:Long, userId:Long)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """messages/$messageId<[^/]+>/favorite""", """controllers.MessagesController.removeFavorite(messageId:Long, userId:Long)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """messages/$messageId<[^/]+>/flag""", """controllers.MessagesController.flag(messageId:Long, userId:Long)"""),
    ("""DELETE""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """messages/$messageId<[^/]+>/flag""", """controllers.MessagesController.removeFlag(messageId:Long, userId:Long)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """users""", """controllers.UsersController.createUser()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """users""", """controllers.UsersController.testCreate()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """auth""", """controllers.UsersController.auth(fbAccessToken:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """devices""", """controllers.DevicesController.createDevice()"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """devices/$deviceId<[^/]+>""", """controllers.DevicesController.updateDeviceInfo(deviceId:Long, regId:String)"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """requests""", """controllers.RequestsController.createRequest()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """requests""", """controllers.RequestsController.getRequestsByReceiver(userId:Long)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """requests/status""", """controllers.RequestsController.getStatus(senderId:Long, receiverId:Long)"""),
    ("""PUT""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """requests/$requestId<[^/]+>""", """controllers.RequestsController.handleResponse(requestId:Long)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """status""", """controllers.StatusController.status()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """test""", """controllers.PublicRoomsController.getRooms()"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:7
  private[this] lazy val controllers_PublicRoomsController_createRoom0_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("publicRooms")))
  )
  private[this] lazy val controllers_PublicRoomsController_createRoom0_invoker = createInvoker(
    PublicRoomsController_0.createRoom(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PublicRoomsController",
      "createRoom",
      Nil,
      "POST",
      """""",
      this.prefix + """publicRooms"""
    )
  )

  // @LINE:8
  private[this] lazy val controllers_PublicRoomsController_getGeoRooms1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("publicRooms")))
  )
  private[this] lazy val controllers_PublicRoomsController_getGeoRooms1_invoker = createInvoker(
    PublicRoomsController_0.getGeoRooms(fakeValue[Double], fakeValue[Double]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PublicRoomsController",
      "getGeoRooms",
      Seq(classOf[Double], classOf[Double]),
      "GET",
      """""",
      this.prefix + """publicRooms"""
    )
  )

  // @LINE:9
  private[this] lazy val controllers_PublicRoomsController_createSubscription2_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("publicRooms/"), DynamicPart("roomId", """[^/]+""",true), StaticPart("/subscriptions")))
  )
  private[this] lazy val controllers_PublicRoomsController_createSubscription2_invoker = createInvoker(
    PublicRoomsController_0.createSubscription(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PublicRoomsController",
      "createSubscription",
      Seq(classOf[Long]),
      "POST",
      """""",
      this.prefix + """publicRooms/$roomId<[^/]+>/subscriptions"""
    )
  )

  // @LINE:10
  private[this] lazy val controllers_PublicRoomsController_removeSubscription3_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("publicRooms/"), DynamicPart("roomId", """[^/]+""",true), StaticPart("/subscriptions/"), DynamicPart("userId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_PublicRoomsController_removeSubscription3_invoker = createInvoker(
    PublicRoomsController_0.removeSubscription(fakeValue[Long], fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PublicRoomsController",
      "removeSubscription",
      Seq(classOf[Long], classOf[Long]),
      "DELETE",
      """""",
      this.prefix + """publicRooms/$roomId<[^/]+>/subscriptions/$userId<[^/]+>"""
    )
  )

  // @LINE:11
  private[this] lazy val controllers_PublicRoomsController_getMessages4_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("publicRooms/"), DynamicPart("roomId", """[^/]+""",true), StaticPart("/messages")))
  )
  private[this] lazy val controllers_PublicRoomsController_getMessages4_invoker = createInvoker(
    PublicRoomsController_0.getMessages(fakeValue[Long], fakeValue[Int], fakeValue[Int]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PublicRoomsController",
      "getMessages",
      Seq(classOf[Long], classOf[Int], classOf[Int]),
      "GET",
      """""",
      this.prefix + """publicRooms/$roomId<[^/]+>/messages"""
    )
  )

  // @LINE:12
  private[this] lazy val controllers_RoomSocketsController_joinPublicRoom5_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("publicRooms/"), DynamicPart("roomId", """[^/]+""",true), StaticPart("/join")))
  )
  private[this] lazy val controllers_RoomSocketsController_joinPublicRoom5_invoker = createInvoker(
    RoomSocketsController_4.joinPublicRoom(fakeValue[Long], fakeValue[Long], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RoomSocketsController",
      "joinPublicRoom",
      Seq(classOf[Long], classOf[Long], classOf[String]),
      "GET",
      """""",
      this.prefix + """publicRooms/$roomId<[^/]+>/join"""
    )
  )

  // @LINE:14
  private[this] lazy val controllers_PrivateRoomsController_getRoomsByUserId6_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("privateRooms")))
  )
  private[this] lazy val controllers_PrivateRoomsController_getRoomsByUserId6_invoker = createInvoker(
    PrivateRoomsController_6.getRoomsByUserId(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PrivateRoomsController",
      "getRoomsByUserId",
      Seq(classOf[Long]),
      "GET",
      """""",
      this.prefix + """privateRooms"""
    )
  )

  // @LINE:15
  private[this] lazy val controllers_PrivateRoomsController_leaveRoom7_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("privateRooms/"), DynamicPart("roomId", """[^/]+""",true), StaticPart("/leave")))
  )
  private[this] lazy val controllers_PrivateRoomsController_leaveRoom7_invoker = createInvoker(
    PrivateRoomsController_6.leaveRoom(fakeValue[Long], fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PrivateRoomsController",
      "leaveRoom",
      Seq(classOf[Long], classOf[Long]),
      "PUT",
      """""",
      this.prefix + """privateRooms/$roomId<[^/]+>/leave"""
    )
  )

  // @LINE:16
  private[this] lazy val controllers_PrivateRoomsController_getMessages8_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("privateRooms/"), DynamicPart("roomId", """[^/]+""",true), StaticPart("/messages")))
  )
  private[this] lazy val controllers_PrivateRoomsController_getMessages8_invoker = createInvoker(
    PrivateRoomsController_6.getMessages(fakeValue[Long], fakeValue[Int], fakeValue[Int]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PrivateRoomsController",
      "getMessages",
      Seq(classOf[Long], classOf[Int], classOf[Int]),
      "GET",
      """""",
      this.prefix + """privateRooms/$roomId<[^/]+>/messages"""
    )
  )

  // @LINE:17
  private[this] lazy val controllers_RoomSocketsController_joinPrivateRoom9_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("privateRooms/"), DynamicPart("roomId", """[^/]+""",true), StaticPart("/join")))
  )
  private[this] lazy val controllers_RoomSocketsController_joinPrivateRoom9_invoker = createInvoker(
    RoomSocketsController_4.joinPrivateRoom(fakeValue[Long], fakeValue[Long], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RoomSocketsController",
      "joinPrivateRoom",
      Seq(classOf[Long], classOf[Long], classOf[String]),
      "GET",
      """""",
      this.prefix + """privateRooms/$roomId<[^/]+>/join"""
    )
  )

  // @LINE:19
  private[this] lazy val controllers_MessagesController_favorite10_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("messages/"), DynamicPart("messageId", """[^/]+""",true), StaticPart("/favorite")))
  )
  private[this] lazy val controllers_MessagesController_favorite10_invoker = createInvoker(
    MessagesController_5.favorite(fakeValue[Long], fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.MessagesController",
      "favorite",
      Seq(classOf[Long], classOf[Long]),
      "PUT",
      """""",
      this.prefix + """messages/$messageId<[^/]+>/favorite"""
    )
  )

  // @LINE:20
  private[this] lazy val controllers_MessagesController_removeFavorite11_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("messages/"), DynamicPart("messageId", """[^/]+""",true), StaticPart("/favorite")))
  )
  private[this] lazy val controllers_MessagesController_removeFavorite11_invoker = createInvoker(
    MessagesController_5.removeFavorite(fakeValue[Long], fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.MessagesController",
      "removeFavorite",
      Seq(classOf[Long], classOf[Long]),
      "DELETE",
      """""",
      this.prefix + """messages/$messageId<[^/]+>/favorite"""
    )
  )

  // @LINE:21
  private[this] lazy val controllers_MessagesController_flag12_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("messages/"), DynamicPart("messageId", """[^/]+""",true), StaticPart("/flag")))
  )
  private[this] lazy val controllers_MessagesController_flag12_invoker = createInvoker(
    MessagesController_5.flag(fakeValue[Long], fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.MessagesController",
      "flag",
      Seq(classOf[Long], classOf[Long]),
      "PUT",
      """""",
      this.prefix + """messages/$messageId<[^/]+>/flag"""
    )
  )

  // @LINE:22
  private[this] lazy val controllers_MessagesController_removeFlag13_route = Route("DELETE",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("messages/"), DynamicPart("messageId", """[^/]+""",true), StaticPart("/flag")))
  )
  private[this] lazy val controllers_MessagesController_removeFlag13_invoker = createInvoker(
    MessagesController_5.removeFlag(fakeValue[Long], fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.MessagesController",
      "removeFlag",
      Seq(classOf[Long], classOf[Long]),
      "DELETE",
      """""",
      this.prefix + """messages/$messageId<[^/]+>/flag"""
    )
  )

  // @LINE:24
  private[this] lazy val controllers_UsersController_createUser14_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("users")))
  )
  private[this] lazy val controllers_UsersController_createUser14_invoker = createInvoker(
    UsersController_2.createUser(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.UsersController",
      "createUser",
      Nil,
      "PUT",
      """""",
      this.prefix + """users"""
    )
  )

  // @LINE:25
  private[this] lazy val controllers_UsersController_testCreate15_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("users")))
  )
  private[this] lazy val controllers_UsersController_testCreate15_invoker = createInvoker(
    UsersController_2.testCreate(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.UsersController",
      "testCreate",
      Nil,
      "POST",
      """""",
      this.prefix + """users"""
    )
  )

  // @LINE:26
  private[this] lazy val controllers_UsersController_auth16_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("auth")))
  )
  private[this] lazy val controllers_UsersController_auth16_invoker = createInvoker(
    UsersController_2.auth(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.UsersController",
      "auth",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """auth"""
    )
  )

  // @LINE:28
  private[this] lazy val controllers_DevicesController_createDevice17_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("devices")))
  )
  private[this] lazy val controllers_DevicesController_createDevice17_invoker = createInvoker(
    DevicesController_3.createDevice(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.DevicesController",
      "createDevice",
      Nil,
      "POST",
      """""",
      this.prefix + """devices"""
    )
  )

  // @LINE:29
  private[this] lazy val controllers_DevicesController_updateDeviceInfo18_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("devices/"), DynamicPart("deviceId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_DevicesController_updateDeviceInfo18_invoker = createInvoker(
    DevicesController_3.updateDeviceInfo(fakeValue[Long], fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.DevicesController",
      "updateDeviceInfo",
      Seq(classOf[Long], classOf[String]),
      "PUT",
      """""",
      this.prefix + """devices/$deviceId<[^/]+>"""
    )
  )

  // @LINE:31
  private[this] lazy val controllers_RequestsController_createRequest19_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("requests")))
  )
  private[this] lazy val controllers_RequestsController_createRequest19_invoker = createInvoker(
    RequestsController_1.createRequest(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RequestsController",
      "createRequest",
      Nil,
      "POST",
      """""",
      this.prefix + """requests"""
    )
  )

  // @LINE:32
  private[this] lazy val controllers_RequestsController_getRequestsByReceiver20_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("requests")))
  )
  private[this] lazy val controllers_RequestsController_getRequestsByReceiver20_invoker = createInvoker(
    RequestsController_1.getRequestsByReceiver(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RequestsController",
      "getRequestsByReceiver",
      Seq(classOf[Long]),
      "GET",
      """""",
      this.prefix + """requests"""
    )
  )

  // @LINE:33
  private[this] lazy val controllers_RequestsController_getStatus21_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("requests/status")))
  )
  private[this] lazy val controllers_RequestsController_getStatus21_invoker = createInvoker(
    RequestsController_1.getStatus(fakeValue[Long], fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RequestsController",
      "getStatus",
      Seq(classOf[Long], classOf[Long]),
      "GET",
      """""",
      this.prefix + """requests/status"""
    )
  )

  // @LINE:34
  private[this] lazy val controllers_RequestsController_handleResponse22_route = Route("PUT",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("requests/"), DynamicPart("requestId", """[^/]+""",true)))
  )
  private[this] lazy val controllers_RequestsController_handleResponse22_invoker = createInvoker(
    RequestsController_1.handleResponse(fakeValue[Long]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.RequestsController",
      "handleResponse",
      Seq(classOf[Long]),
      "PUT",
      """""",
      this.prefix + """requests/$requestId<[^/]+>"""
    )
  )

  // @LINE:36
  private[this] lazy val controllers_StatusController_status23_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("status")))
  )
  private[this] lazy val controllers_StatusController_status23_invoker = createInvoker(
    StatusController_7.status(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.StatusController",
      "status",
      Nil,
      "GET",
      """""",
      this.prefix + """status"""
    )
  )

  // @LINE:37
  private[this] lazy val controllers_PublicRoomsController_getRooms24_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("test")))
  )
  private[this] lazy val controllers_PublicRoomsController_getRooms24_invoker = createInvoker(
    PublicRoomsController_0.getRooms(),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.PublicRoomsController",
      "getRooms",
      Nil,
      "GET",
      """""",
      this.prefix + """test"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:7
    case controllers_PublicRoomsController_createRoom0_route(params) =>
      call { 
        controllers_PublicRoomsController_createRoom0_invoker.call(PublicRoomsController_0.createRoom())
      }
  
    // @LINE:8
    case controllers_PublicRoomsController_getGeoRooms1_route(params) =>
      call(params.fromQuery[Double]("lat", None), params.fromQuery[Double]("lon", None)) { (lat, lon) =>
        controllers_PublicRoomsController_getGeoRooms1_invoker.call(PublicRoomsController_0.getGeoRooms(lat, lon))
      }
  
    // @LINE:9
    case controllers_PublicRoomsController_createSubscription2_route(params) =>
      call(params.fromPath[Long]("roomId", None)) { (roomId) =>
        controllers_PublicRoomsController_createSubscription2_invoker.call(PublicRoomsController_0.createSubscription(roomId))
      }
  
    // @LINE:10
    case controllers_PublicRoomsController_removeSubscription3_route(params) =>
      call(params.fromPath[Long]("roomId", None), params.fromPath[Long]("userId", None)) { (roomId, userId) =>
        controllers_PublicRoomsController_removeSubscription3_invoker.call(PublicRoomsController_0.removeSubscription(roomId, userId))
      }
  
    // @LINE:11
    case controllers_PublicRoomsController_getMessages4_route(params) =>
      call(params.fromPath[Long]("roomId", None), params.fromQuery[Int]("limit", None), params.fromQuery[Int]("offset", None)) { (roomId, limit, offset) =>
        controllers_PublicRoomsController_getMessages4_invoker.call(PublicRoomsController_0.getMessages(roomId, limit, offset))
      }
  
    // @LINE:12
    case controllers_RoomSocketsController_joinPublicRoom5_route(params) =>
      call(params.fromPath[Long]("roomId", None), params.fromQuery[Long]("userId", None), params.fromQuery[String]("authToken", None)) { (roomId, userId, authToken) =>
        controllers_RoomSocketsController_joinPublicRoom5_invoker.call(RoomSocketsController_4.joinPublicRoom(roomId, userId, authToken))
      }
  
    // @LINE:14
    case controllers_PrivateRoomsController_getRoomsByUserId6_route(params) =>
      call(params.fromQuery[Long]("userId", None)) { (userId) =>
        controllers_PrivateRoomsController_getRoomsByUserId6_invoker.call(PrivateRoomsController_6.getRoomsByUserId(userId))
      }
  
    // @LINE:15
    case controllers_PrivateRoomsController_leaveRoom7_route(params) =>
      call(params.fromPath[Long]("roomId", None), params.fromQuery[Long]("userId", None)) { (roomId, userId) =>
        controllers_PrivateRoomsController_leaveRoom7_invoker.call(PrivateRoomsController_6.leaveRoom(roomId, userId))
      }
  
    // @LINE:16
    case controllers_PrivateRoomsController_getMessages8_route(params) =>
      call(params.fromPath[Long]("roomId", None), params.fromQuery[Int]("limit", None), params.fromQuery[Int]("offset", None)) { (roomId, limit, offset) =>
        controllers_PrivateRoomsController_getMessages8_invoker.call(PrivateRoomsController_6.getMessages(roomId, limit, offset))
      }
  
    // @LINE:17
    case controllers_RoomSocketsController_joinPrivateRoom9_route(params) =>
      call(params.fromPath[Long]("roomId", None), params.fromQuery[Long]("userId", None), params.fromQuery[String]("authToken", None)) { (roomId, userId, authToken) =>
        controllers_RoomSocketsController_joinPrivateRoom9_invoker.call(RoomSocketsController_4.joinPrivateRoom(roomId, userId, authToken))
      }
  
    // @LINE:19
    case controllers_MessagesController_favorite10_route(params) =>
      call(params.fromPath[Long]("messageId", None), params.fromQuery[Long]("userId", None)) { (messageId, userId) =>
        controllers_MessagesController_favorite10_invoker.call(MessagesController_5.favorite(messageId, userId))
      }
  
    // @LINE:20
    case controllers_MessagesController_removeFavorite11_route(params) =>
      call(params.fromPath[Long]("messageId", None), params.fromQuery[Long]("userId", None)) { (messageId, userId) =>
        controllers_MessagesController_removeFavorite11_invoker.call(MessagesController_5.removeFavorite(messageId, userId))
      }
  
    // @LINE:21
    case controllers_MessagesController_flag12_route(params) =>
      call(params.fromPath[Long]("messageId", None), params.fromQuery[Long]("userId", None)) { (messageId, userId) =>
        controllers_MessagesController_flag12_invoker.call(MessagesController_5.flag(messageId, userId))
      }
  
    // @LINE:22
    case controllers_MessagesController_removeFlag13_route(params) =>
      call(params.fromPath[Long]("messageId", None), params.fromQuery[Long]("userId", None)) { (messageId, userId) =>
        controllers_MessagesController_removeFlag13_invoker.call(MessagesController_5.removeFlag(messageId, userId))
      }
  
    // @LINE:24
    case controllers_UsersController_createUser14_route(params) =>
      call { 
        controllers_UsersController_createUser14_invoker.call(UsersController_2.createUser())
      }
  
    // @LINE:25
    case controllers_UsersController_testCreate15_route(params) =>
      call { 
        controllers_UsersController_testCreate15_invoker.call(UsersController_2.testCreate())
      }
  
    // @LINE:26
    case controllers_UsersController_auth16_route(params) =>
      call(params.fromQuery[String]("fbAccessToken", None)) { (fbAccessToken) =>
        controllers_UsersController_auth16_invoker.call(UsersController_2.auth(fbAccessToken))
      }
  
    // @LINE:28
    case controllers_DevicesController_createDevice17_route(params) =>
      call { 
        controllers_DevicesController_createDevice17_invoker.call(DevicesController_3.createDevice())
      }
  
    // @LINE:29
    case controllers_DevicesController_updateDeviceInfo18_route(params) =>
      call(params.fromPath[Long]("deviceId", None), params.fromQuery[String]("regId", None)) { (deviceId, regId) =>
        controllers_DevicesController_updateDeviceInfo18_invoker.call(DevicesController_3.updateDeviceInfo(deviceId, regId))
      }
  
    // @LINE:31
    case controllers_RequestsController_createRequest19_route(params) =>
      call { 
        controllers_RequestsController_createRequest19_invoker.call(RequestsController_1.createRequest())
      }
  
    // @LINE:32
    case controllers_RequestsController_getRequestsByReceiver20_route(params) =>
      call(params.fromQuery[Long]("userId", None)) { (userId) =>
        controllers_RequestsController_getRequestsByReceiver20_invoker.call(RequestsController_1.getRequestsByReceiver(userId))
      }
  
    // @LINE:33
    case controllers_RequestsController_getStatus21_route(params) =>
      call(params.fromQuery[Long]("senderId", None), params.fromQuery[Long]("receiverId", None)) { (senderId, receiverId) =>
        controllers_RequestsController_getStatus21_invoker.call(RequestsController_1.getStatus(senderId, receiverId))
      }
  
    // @LINE:34
    case controllers_RequestsController_handleResponse22_route(params) =>
      call(params.fromPath[Long]("requestId", None)) { (requestId) =>
        controllers_RequestsController_handleResponse22_invoker.call(RequestsController_1.handleResponse(requestId))
      }
  
    // @LINE:36
    case controllers_StatusController_status23_route(params) =>
      call { 
        controllers_StatusController_status23_invoker.call(StatusController_7.status())
      }
  
    // @LINE:37
    case controllers_PublicRoomsController_getRooms24_route(params) =>
      call { 
        controllers_PublicRoomsController_getRooms24_invoker.call(PublicRoomsController_0.getRooms())
      }
  }
}