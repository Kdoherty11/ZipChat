package unit.controllers;

import com.google.common.collect.ImmutableMap;
import controllers.BaseController;
import controllers.RequestsController;
import models.Request;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import services.AbstractUserService;
import services.RequestService;
import services.SecurityService;
import services.UserService;
import validation.validators.RequiredValidator;
import validation.validators.StringToLongValidator;
import validation.validators.WhiteListValidator;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static play.libs.Json.toJson;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.test.Helpers.OK;
import static play.test.Helpers.*;

/**
 * Created by kdoherty on 7/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestsControllerTest {

    private RequestsController controller;

    @Mock
    private RequestService requestService;

    @Mock
    private AbstractUserService abstractUserService;

    @Mock
    private UserService userService;

    @Mock
    private SecurityService securityService;

    private Helpers helpers;

    @Before
    public void setUp() {
        controller = new RequestsController(requestService, abstractUserService,
                userService, securityService);
        helpers = new Helpers();

        start(fakeApplication());
    }

    @Test
    public void createRequestNoSender() {
        Map<String, String> bodyForm = ImmutableMap.of("receiver", "1");
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createRequest);

        assertEquals(BAD_REQUEST, result.status());
        String resultString = contentAsString(result);
        assertTrue(resultString.contains("sender"));
        assertTrue(resultString.contains(RequiredValidator.ERROR_MESSAGE));
    }

    @Test
    public void createRequestInvalidSender() {
        Map<String, String> bodyForm = ImmutableMap.of("receiver", "1", "sender", "notALong");
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createRequest);

        assertEquals(BAD_REQUEST, result.status());
        String resultString = contentAsString(result);
        assertTrue(resultString.contains("sender"));
        assertTrue(resultString.contains(StringToLongValidator.ERROR_MESSAGE));
    }

    @Test
    public void createRequestNoReceiver() {
        Map<String, String> bodyForm = ImmutableMap.of("sender", "1");
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createRequest);

        assertEquals(BAD_REQUEST, result.status());
        String resultString = contentAsString(result);
        assertTrue(resultString.contains("receiver"));
        assertTrue(resultString.contains(RequiredValidator.ERROR_MESSAGE));
    }

    @Test
    public void createRequestInvalidReceiver() {
        Map<String, String> bodyForm = ImmutableMap.of("sender", "1", "receiver", "notALong");
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createRequest);

        assertEquals(BAD_REQUEST, result.status());
        String resultString = contentAsString(result);
        assertTrue(resultString.contains("receiver"));
        assertTrue(resultString.contains(StringToLongValidator.ERROR_MESSAGE));
    }

    @Test
    public void createRequestIsSecuredBySender() {
        long senderId = 1;
        when(securityService.isUnauthorized(senderId)).thenReturn(true);

        Map<String, String> bodyForm = ImmutableMap.of("sender", Long.toString(senderId), "receiver", "2");
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createRequest);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void createRequestSenderNotFound() {
        long senderId = 1;
        when(securityService.isUnauthorized(senderId)).thenReturn(false);
        when(userService.findById(senderId)).thenReturn(Optional.empty());

        Map<String, String> bodyForm = ImmutableMap.of("sender", Long.toString(senderId), "receiver", "2");
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createRequest);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(User.class, senderId)));
    }

    @Test
    public void createRequestReceiverNotFound() {
        long senderId = 1;
        long receiverId = 2;
        when(securityService.isUnauthorized(senderId)).thenReturn(false);
        User sender = mock(User.class);
        when(userService.findById(senderId)).thenReturn(Optional.of(sender));
        when(abstractUserService.findById(receiverId)).thenReturn(Optional.empty());

        Map<String, String> bodyForm = ImmutableMap.of("sender", Long.toString(senderId), "receiver", Long.toString(receiverId));
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createRequest);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(User.class, receiverId)));
    }

    @Test
    public void createRequestSendsChatRequestToReceiverFromSender() {
        long senderId = 1;
        long receiverId = 2;
        when(securityService.isUnauthorized(senderId)).thenReturn(false);
        User sender = mock(User.class);
        User receiver = mock(User.class);
        when(userService.findById(senderId)).thenReturn(Optional.of(sender));
        when(abstractUserService.findById(receiverId)).thenReturn(Optional.of(receiver));

        Map<String, String> bodyForm = ImmutableMap.of("sender", Long.toString(senderId), "receiver", Long.toString(receiverId));
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        helpers.invokeWithContext(builder, controller::createRequest);

        verify(userService).sendChatRequest(sender, receiver);
    }

    @Test
    public void createRequestReturnsOkResult() {
        long senderId = 1;
        long receiverId = 2;
        when(securityService.isUnauthorized(senderId)).thenReturn(false);
        User sender = mock(User.class);
        User receiver = mock(User.class);
        when(userService.findById(senderId)).thenReturn(Optional.of(sender));
        when(abstractUserService.findById(receiverId)).thenReturn(Optional.of(receiver));

        Map<String, String> bodyForm = ImmutableMap.of("sender", Long.toString(senderId), "receiver", Long.toString(receiverId));
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, controller::createRequest);

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(BaseController.OK_STRING));
    }

    @Test
    public void getRequestsByReceiverIsSecured() {
        long receiverId = 1;
        when(securityService.isUnauthorized(receiverId)).thenReturn(true);

        Result result = controller.getRequestsByReceiver(receiverId);

        assertEquals(FORBIDDEN, result.status());
        verifyZeroInteractions(requestService);
    }

    @Test
    public void getRequestsByReceiverReturnsRequests() {
        long receiverId = 1;
        when(securityService.isUnauthorized(receiverId)).thenReturn(false);
        List<Request> requests = new ArrayList<>();
        when(requestService.findPendingRequestsByReceiver(receiverId)).thenReturn(requests);

        Result result = controller.getRequestsByReceiver(receiverId);

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(toJson(requests).asText()));
    }

    @Test
    public void handleResponseNoResponse() {
        Map<String, String> bodyForm = Collections.emptyMap();
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, () -> controller.handleResponse(1));

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains(RequiredValidator.ERROR_MESSAGE));
    }

    @Test
    public void handleResponseInvalidResponse() {
        Map<String, String> bodyForm = ImmutableMap.of("status", "notAcceptedOrDenied");
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, () -> controller.handleResponse(1));

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains(WhiteListValidator.ERROR_MESSAGE));
    }

    @Test
    public void handleResponseNoRequest() {
        long requestId = 1;
        when(requestService.findById(requestId)).thenReturn(Optional.empty());

        Map<String, String> bodyForm = ImmutableMap.of("status", Request.Status.accepted.name());
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, () -> controller.handleResponse(requestId));

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(BaseController.buildEntityNotFoundString(Request.class, requestId)));
    }

    @Test
    public void handleResponseIsSecuredByRequestReceiverId() {
        long requestId = 1;
        long requestReceiverUserId = 2;
        Request request = mock(Request.class);
        User requestReceiver = mock(User.class);

        when(request.receiver).thenReturn(requestReceiver);
        when(requestReceiver.userId).thenReturn(requestReceiverUserId);
        when(requestService.findById(requestId)).thenReturn(Optional.of(request));
        when(securityService.isUnauthorized(requestReceiverUserId)).thenReturn(true);

        Map<String, String> bodyForm = ImmutableMap.of("status", Request.Status.accepted.name());
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, () -> controller.handleResponse(requestId));

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void handleResponseOnNonPendingRequest() {
        long requestId = 1;
        long requestReceiverUserId = 2;
        Request request = mock(Request.class);
        User requestReceiver = mock(User.class);

        when(request.receiver).thenReturn(requestReceiver);
        when(request.status).thenReturn(Request.Status.denied);
        when(requestReceiver.userId).thenReturn(requestReceiverUserId);
        when(requestService.findById(requestId)).thenReturn(Optional.of(request));
        when(securityService.isUnauthorized(requestReceiverUserId)).thenReturn(false);

        Map<String, String> bodyForm = ImmutableMap.of("status", Request.Status.accepted.name());
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, () -> controller.handleResponse(requestId));

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("This request has already been responded to"));
    }

    @Test
    public void handleResponseCallsServiceHandleResponseWithRequestAndResponse() {
        long requestId = 1;
        long requestReceiverUserId = 2;
        Request.Status response = Request.Status.accepted;
        Request request = mock(Request.class);
        User requestReceiver = mock(User.class);

        when(request.receiver).thenReturn(requestReceiver);
        when(request.status).thenReturn(Request.Status.pending);
        when(requestReceiver.userId).thenReturn(requestReceiverUserId);
        when(requestService.findById(requestId)).thenReturn(Optional.of(request));
        when(securityService.isUnauthorized(requestReceiverUserId)).thenReturn(false);

        Map<String, String> bodyForm = ImmutableMap.of("status", response.name());
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        helpers.invokeWithContext(builder, () -> controller.handleResponse(requestId));

        verify(requestService).handleResponse(request, response);
    }

    @Test
    public void handleResponseReturnsOk() {
        long requestId = 1;
        long requestReceiverUserId = 2;
        Request.Status response = Request.Status.accepted;
        Request request = mock(Request.class);
        User requestReceiver = mock(User.class);

        when(request.receiver).thenReturn(requestReceiver);
        when(request.status).thenReturn(Request.Status.pending);
        when(requestReceiver.userId).thenReturn(requestReceiverUserId);
        when(requestService.findById(requestId)).thenReturn(Optional.of(request));
        when(securityService.isUnauthorized(requestReceiverUserId)).thenReturn(false);

        Map<String, String> bodyForm = ImmutableMap.of("status", response.name());
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(bodyForm);
        Result result = helpers.invokeWithContext(builder, () -> controller.handleResponse(requestId));

        assertEquals(OK, result.status());
    }

    @Test
    public void getStatusCallsThroughToTheRequestServiceAndReturnsOk() {
        long senderId = 1;
        long receiverId = 2;
        String fakeStatus = "ThisIsMyFakeStatus";
        when(requestService.getStatus(senderId, receiverId)).thenReturn(fakeStatus);

        Result result = controller.getStatus(senderId, receiverId);

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(fakeStatus));
    }

}
