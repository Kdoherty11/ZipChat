
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kdoherty/dev/ideaProjects/ZipChat/conf/routes
// @DATE:Fri Aug 07 17:18:08 EDT 2015

import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:7
package controllers {

  // @LINE:31
  class ReverseRequestsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:34
    def handleResponse(requestId:Long): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "requests/" + implicitly[PathBindable[Long]].unbind("requestId", requestId))
    }
  
    // @LINE:31
    def createRequest(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "requests")
    }
  
    // @LINE:33
    def getStatus(senderId:Long, receiverId:Long): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "requests/status" + queryString(List(Some(implicitly[QueryStringBindable[Long]].unbind("senderId", senderId)), Some(implicitly[QueryStringBindable[Long]].unbind("receiverId", receiverId)))))
    }
  
    // @LINE:32
    def getRequestsByReceiver(userId:Long): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "requests" + queryString(List(Some(implicitly[QueryStringBindable[Long]].unbind("userId", userId)))))
    }
  
  }

  // @LINE:12
  class ReverseRoomSocketsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:17
    def joinPrivateRoom(roomId:Long, userId:Long, authToken:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "privateRooms/" + implicitly[PathBindable[Long]].unbind("roomId", roomId) + "/join" + queryString(List(Some(implicitly[QueryStringBindable[Long]].unbind("userId", userId)), Some(implicitly[QueryStringBindable[String]].unbind("authToken", authToken)))))
    }
  
    // @LINE:12
    def joinPublicRoom(roomId:Long, userId:Long, authToken:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "publicRooms/" + implicitly[PathBindable[Long]].unbind("roomId", roomId) + "/join" + queryString(List(Some(implicitly[QueryStringBindable[Long]].unbind("userId", userId)), Some(implicitly[QueryStringBindable[String]].unbind("authToken", authToken)))))
    }
  
  }

  // @LINE:28
  class ReverseDevicesController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:28
    def createDevice(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "devices")
    }
  
    // @LINE:29
    def updateDeviceInfo(deviceId:Long, regId:String): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "devices/" + implicitly[PathBindable[Long]].unbind("deviceId", deviceId) + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("regId", regId)))))
    }
  
  }

  // @LINE:24
  class ReverseUsersController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:26
    def auth(fbAccessToken:String): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "auth" + queryString(List(Some(implicitly[QueryStringBindable[String]].unbind("fbAccessToken", fbAccessToken)))))
    }
  
    // @LINE:25
    def testCreate(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "users")
    }
  
    // @LINE:24
    def createUser(): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "users")
    }
  
  }

  // @LINE:7
  class ReversePublicRoomsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:10
    def removeSubscription(roomId:Long, userId:Long): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "publicRooms/" + implicitly[PathBindable[Long]].unbind("roomId", roomId) + "/subscriptions/" + implicitly[PathBindable[Long]].unbind("userId", userId))
    }
  
    // @LINE:8
    def getGeoRooms(lat:Double, lon:Double): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "publicRooms" + queryString(List(Some(implicitly[QueryStringBindable[Double]].unbind("lat", lat)), Some(implicitly[QueryStringBindable[Double]].unbind("lon", lon)))))
    }
  
    // @LINE:7
    def createRoom(): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "publicRooms")
    }
  
    // @LINE:9
    def createSubscription(roomId:Long): Call = {
      import ReverseRouteContext.empty
      Call("POST", _prefix + { _defaultPrefix } + "publicRooms/" + implicitly[PathBindable[Long]].unbind("roomId", roomId) + "/subscriptions")
    }
  
    // @LINE:11
    def getMessages(roomId:Long, limit:Int, offset:Int): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "publicRooms/" + implicitly[PathBindable[Long]].unbind("roomId", roomId) + "/messages" + queryString(List(Some(implicitly[QueryStringBindable[Int]].unbind("limit", limit)), Some(implicitly[QueryStringBindable[Int]].unbind("offset", offset)))))
    }
  
    // @LINE:37
    def getRooms(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "test")
    }
  
  }

  // @LINE:36
  class ReverseStatusController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:36
    def status(): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "status")
    }
  
  }

  // @LINE:14
  class ReversePrivateRoomsController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:16
    def getMessages(roomId:Long, limit:Int, offset:Int): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "privateRooms/" + implicitly[PathBindable[Long]].unbind("roomId", roomId) + "/messages" + queryString(List(Some(implicitly[QueryStringBindable[Int]].unbind("limit", limit)), Some(implicitly[QueryStringBindable[Int]].unbind("offset", offset)))))
    }
  
    // @LINE:15
    def leaveRoom(roomId:Long, userId:Long): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "privateRooms/" + implicitly[PathBindable[Long]].unbind("roomId", roomId) + "/leave" + queryString(List(Some(implicitly[QueryStringBindable[Long]].unbind("userId", userId)))))
    }
  
    // @LINE:14
    def getRoomsByUserId(userId:Long): Call = {
      import ReverseRouteContext.empty
      Call("GET", _prefix + { _defaultPrefix } + "privateRooms" + queryString(List(Some(implicitly[QueryStringBindable[Long]].unbind("userId", userId)))))
    }
  
  }

  // @LINE:19
  class ReverseMessagesController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:21
    def flag(messageId:Long, userId:Long): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "messages/" + implicitly[PathBindable[Long]].unbind("messageId", messageId) + "/flag" + queryString(List(Some(implicitly[QueryStringBindable[Long]].unbind("userId", userId)))))
    }
  
    // @LINE:22
    def removeFlag(messageId:Long, userId:Long): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "messages/" + implicitly[PathBindable[Long]].unbind("messageId", messageId) + "/flag" + queryString(List(Some(implicitly[QueryStringBindable[Long]].unbind("userId", userId)))))
    }
  
    // @LINE:19
    def favorite(messageId:Long, userId:Long): Call = {
      import ReverseRouteContext.empty
      Call("PUT", _prefix + { _defaultPrefix } + "messages/" + implicitly[PathBindable[Long]].unbind("messageId", messageId) + "/favorite" + queryString(List(Some(implicitly[QueryStringBindable[Long]].unbind("userId", userId)))))
    }
  
    // @LINE:20
    def removeFavorite(messageId:Long, userId:Long): Call = {
      import ReverseRouteContext.empty
      Call("DELETE", _prefix + { _defaultPrefix } + "messages/" + implicitly[PathBindable[Long]].unbind("messageId", messageId) + "/favorite" + queryString(List(Some(implicitly[QueryStringBindable[Long]].unbind("userId", userId)))))
    }
  
  }


}