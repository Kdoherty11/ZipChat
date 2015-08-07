
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/kdoherty/dev/ideaProjects/ZipChat/conf/routes
// @DATE:Fri Aug 07 17:18:08 EDT 2015

import play.api.routing.JavaScriptReverseRoute
import play.api.mvc.{ QueryStringBindable, PathBindable, Call, JavascriptLiteral }
import play.core.routing.{ HandlerDef, ReverseRouteContext, queryString, dynamicString }


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:7
package controllers.javascript {
  import ReverseRouteContext.empty

  // @LINE:31
  class ReverseRequestsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:34
    def handleResponse: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RequestsController.handleResponse",
      """
        function(requestId) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "requests/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("requestId", requestId)})
        }
      """
    )
  
    // @LINE:31
    def createRequest: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RequestsController.createRequest",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "requests"})
        }
      """
    )
  
    // @LINE:33
    def getStatus: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RequestsController.getStatus",
      """
        function(senderId,receiverId) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "requests/status" + _qS([(""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("senderId", senderId), (""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("receiverId", receiverId)])})
        }
      """
    )
  
    // @LINE:32
    def getRequestsByReceiver: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RequestsController.getRequestsByReceiver",
      """
        function(userId) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "requests" + _qS([(""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("userId", userId)])})
        }
      """
    )
  
  }

  // @LINE:12
  class ReverseRoomSocketsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:17
    def joinPrivateRoom: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RoomSocketsController.joinPrivateRoom",
      """
        function(roomId,userId,authToken) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "privateRooms/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("roomId", roomId) + "/join" + _qS([(""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("userId", userId), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("authToken", authToken)])})
        }
      """
    )
  
    // @LINE:12
    def joinPublicRoom: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.RoomSocketsController.joinPublicRoom",
      """
        function(roomId,userId,authToken) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "publicRooms/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("roomId", roomId) + "/join" + _qS([(""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("userId", userId), (""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("authToken", authToken)])})
        }
      """
    )
  
  }

  // @LINE:28
  class ReverseDevicesController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:28
    def createDevice: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DevicesController.createDevice",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "devices"})
        }
      """
    )
  
    // @LINE:29
    def updateDeviceInfo: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.DevicesController.updateDeviceInfo",
      """
        function(deviceId,regId) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "devices/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("deviceId", deviceId) + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("regId", regId)])})
        }
      """
    )
  
  }

  // @LINE:24
  class ReverseUsersController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:26
    def auth: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.UsersController.auth",
      """
        function(fbAccessToken) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "auth" + _qS([(""" + implicitly[QueryStringBindable[String]].javascriptUnbind + """)("fbAccessToken", fbAccessToken)])})
        }
      """
    )
  
    // @LINE:25
    def testCreate: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.UsersController.testCreate",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "users"})
        }
      """
    )
  
    // @LINE:24
    def createUser: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.UsersController.createUser",
      """
        function() {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "users"})
        }
      """
    )
  
  }

  // @LINE:7
  class ReversePublicRoomsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:10
    def removeSubscription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PublicRoomsController.removeSubscription",
      """
        function(roomId,userId) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "publicRooms/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("roomId", roomId) + "/subscriptions/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("userId", userId)})
        }
      """
    )
  
    // @LINE:8
    def getGeoRooms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PublicRoomsController.getGeoRooms",
      """
        function(lat,lon) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "publicRooms" + _qS([(""" + implicitly[QueryStringBindable[Double]].javascriptUnbind + """)("lat", lat), (""" + implicitly[QueryStringBindable[Double]].javascriptUnbind + """)("lon", lon)])})
        }
      """
    )
  
    // @LINE:7
    def createRoom: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PublicRoomsController.createRoom",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "publicRooms"})
        }
      """
    )
  
    // @LINE:9
    def createSubscription: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PublicRoomsController.createSubscription",
      """
        function(roomId) {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "publicRooms/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("roomId", roomId) + "/subscriptions"})
        }
      """
    )
  
    // @LINE:11
    def getMessages: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PublicRoomsController.getMessages",
      """
        function(roomId,limit,offset) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "publicRooms/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("roomId", roomId) + "/messages" + _qS([(""" + implicitly[QueryStringBindable[Int]].javascriptUnbind + """)("limit", limit), (""" + implicitly[QueryStringBindable[Int]].javascriptUnbind + """)("offset", offset)])})
        }
      """
    )
  
    // @LINE:37
    def getRooms: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PublicRoomsController.getRooms",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "test"})
        }
      """
    )
  
  }

  // @LINE:36
  class ReverseStatusController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:36
    def status: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.StatusController.status",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "status"})
        }
      """
    )
  
  }

  // @LINE:14
  class ReversePrivateRoomsController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:16
    def getMessages: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PrivateRoomsController.getMessages",
      """
        function(roomId,limit,offset) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "privateRooms/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("roomId", roomId) + "/messages" + _qS([(""" + implicitly[QueryStringBindable[Int]].javascriptUnbind + """)("limit", limit), (""" + implicitly[QueryStringBindable[Int]].javascriptUnbind + """)("offset", offset)])})
        }
      """
    )
  
    // @LINE:15
    def leaveRoom: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PrivateRoomsController.leaveRoom",
      """
        function(roomId,userId) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "privateRooms/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("roomId", roomId) + "/leave" + _qS([(""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("userId", userId)])})
        }
      """
    )
  
    // @LINE:14
    def getRoomsByUserId: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.PrivateRoomsController.getRoomsByUserId",
      """
        function(userId) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "privateRooms" + _qS([(""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("userId", userId)])})
        }
      """
    )
  
  }

  // @LINE:19
  class ReverseMessagesController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:21
    def flag: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.MessagesController.flag",
      """
        function(messageId,userId) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "messages/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("messageId", messageId) + "/flag" + _qS([(""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("userId", userId)])})
        }
      """
    )
  
    // @LINE:22
    def removeFlag: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.MessagesController.removeFlag",
      """
        function(messageId,userId) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "messages/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("messageId", messageId) + "/flag" + _qS([(""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("userId", userId)])})
        }
      """
    )
  
    // @LINE:19
    def favorite: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.MessagesController.favorite",
      """
        function(messageId,userId) {
          return _wA({method:"PUT", url:"""" + _prefix + { _defaultPrefix } + """" + "messages/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("messageId", messageId) + "/favorite" + _qS([(""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("userId", userId)])})
        }
      """
    )
  
    // @LINE:20
    def removeFavorite: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.MessagesController.removeFavorite",
      """
        function(messageId,userId) {
          return _wA({method:"DELETE", url:"""" + _prefix + { _defaultPrefix } + """" + "messages/" + (""" + implicitly[PathBindable[Long]].javascriptUnbind + """)("messageId", messageId) + "/favorite" + _qS([(""" + implicitly[QueryStringBindable[Long]].javascriptUnbind + """)("userId", userId)])})
        }
      """
    )
  
  }


}