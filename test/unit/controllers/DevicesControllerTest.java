package unit.controllers;

import com.google.common.collect.ImmutableMap;
import controllers.DevicesController;
import models.Device;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import services.DeviceService;
import services.SecurityService;
import services.UserService;
import utils.DbUtils;
import validation.validators.RequiredValidator;
import validation.validators.StringToLongValidator;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.test.Helpers.*;

/**
 * Created by kdoherty on 7/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class DevicesControllerTest {

    private DevicesController controller;

    @Mock
    private DeviceService deviceService;

    @Mock
    private UserService userService;

    @Mock
    private SecurityService securityService;

    private Helpers helpers;

    @Before
    public void setUp() {
        helpers = new Helpers();
        controller = new DevicesController(deviceService, userService, securityService);
        start(fakeApplication());
    }

    @Test
    public void createDeviceWithoutUserId() {
        Map<String, String> requestBody = Collections.emptyMap();
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(requestBody);

        Result result = helpers.invokeWithContext(builder, controller::createDevice);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("userId"));
        assertTrue(contentAsString(result).contains("This field is required"));
        verifyZeroInteractions(userService, deviceService);
    }

    @Test
    public void createDeviceWithInvalidUserId() {
        Map<String, String> requestBody = ImmutableMap.of("userId", "notALong");
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(requestBody);

        Result result = helpers.invokeWithContext(builder, controller::createDevice);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains(StringToLongValidator.ERROR_MESSAGE));
        verifyZeroInteractions(userService, deviceService);
    }

    @Test
    public void createDeviceIsSecured() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(true);
        Map<String, String> requestBody = ImmutableMap.of("userId", Long.toString(userId), "regId", "myRegId", "platform", Device.Platform.android.name());
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(requestBody);

        Result result = helpers.invokeWithContext(builder, controller::createDevice);

        assertEquals(FORBIDDEN, result.status());
        verifyZeroInteractions(userService, deviceService);
    }

    @Test
    public void createDeviceWithoutRegId() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        Map<String, String> requestBody = ImmutableMap.of("userId", Long.toString(userId), "platform", Device.Platform.android.name());
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(requestBody);

        Result result = helpers.invokeWithContext(builder, controller::createDevice);

        assertEquals(BAD_REQUEST, result.status());
        String resultString = contentAsString(result);
        assertTrue(resultString.contains(RequiredValidator.ERROR_MESSAGE));
        assertTrue(resultString.contains("regId"));
        verifyZeroInteractions(userService, deviceService);
    }

    @Test
    public void createDeviceWithoutPlatform() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        Map<String, String> requestBody = ImmutableMap.of("userId", Long.toString(userId), "regId", "myRegId");
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(requestBody);

        Result result = helpers.invokeWithContext(builder, controller::createDevice);

        assertEquals(BAD_REQUEST, result.status());
        String resultString = contentAsString(result);
        assertTrue(resultString.contains(RequiredValidator.ERROR_MESSAGE));
        assertTrue(resultString.contains("platform"));
        verifyZeroInteractions(userService, deviceService);
    }

    @Test
    public void createDeviceUserNotFound() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        when(userService.findById(userId)).thenReturn(Optional.empty());
        Map<String, String> requestBody = ImmutableMap.of("userId", Long.toString(userId), "regId", "myRegId",
                "platform", Device.Platform.android.name());
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(requestBody);

        Result result = helpers.invokeWithContext(builder, controller::createDevice);

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(DbUtils.buildEntityNotFoundString(User.class, userId)));
        verifyZeroInteractions(deviceService);
    }

    @Test
    public void createDeviceSavesDeviceWithUserIdRegIdAndPlatform() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        User user = mock(User.class);
        String regId = "myRegId";
        Device.Platform platform = Device.Platform.android;
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        Map<String, String> requestBody = ImmutableMap.of("userId", Long.toString(userId), "regId", regId,
                "platform", platform.name());
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(requestBody);

        helpers.invokeWithContext(builder, controller::createDevice);

        Device device = new Device(user, regId, platform);

        verify(deviceService).save(eq(device));
    }

    @Test
    public void createDeviceRespondsWithTheDeviceId() {
        long userId = 1;
        when(securityService.isUnauthorized(userId)).thenReturn(false);
        User user = mock(User.class);
        String regId = "myRegId";
        Device.Platform platform = Device.Platform.android;
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        Map<String, String> requestBody = ImmutableMap.of("userId", Long.toString(userId), "regId", regId,
                "platform", platform.name());
        Http.RequestBuilder builder = new Http.RequestBuilder().bodyForm(requestBody);

        Result result = helpers.invokeWithContext(builder, controller::createDevice);

        assertEquals(CREATED, result.status());
        assertTrue(contentAsString(result).contains("deviceId"));
    }

    @Test
    public void updateDeviceInfoDeviceNotFound() {
        long deviceId = 1;
        when(deviceService.findById(deviceId)).thenReturn(Optional.empty());

        Result result = controller.updateDeviceInfo(deviceId, "regId");

        assertEquals(NOT_FOUND, result.status());
        assertTrue(contentAsString(result).contains(
                DbUtils.buildEntityNotFoundString(Device.class, deviceId)));
    }

    @Test
    public void updateDeviceInfoIsSecured() {
        long deviceId = 1;
        long userId = 2;
        Device device = mock(Device.class);
        User user = mock(User.class);
        when(device.user).thenReturn(user);
        when(user.userId).thenReturn(userId);
        when(deviceService.findById(deviceId)).thenReturn(Optional.of(device));
        when(securityService.isUnauthorized(userId)).thenReturn(true);

        Result result = controller.updateDeviceInfo(deviceId, "regId");

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void updateDeviceInfoUpdatesTheRegId() {
        long deviceId = 1;
        long userId = 2;
        User user = mock(User.class);
        when(user.userId).thenReturn(userId);
        Device device = new Device(user, "regId", Device.Platform.android);
        when(deviceService.findById(deviceId)).thenReturn(Optional.of(device));
        when(securityService.isUnauthorized(userId)).thenReturn(false);

        String newRegId = "newRegId";
        controller.updateDeviceInfo(deviceId, newRegId);

        assertEquals(newRegId, device.regId);
    }

    @Test
    public void updateDeviceInfoDoesNotUpdateTheRegIdIfItHasNotChanged() {
        long deviceId = 1;
        long userId = 2;
        User user = mock(User.class);
        when(user.userId).thenReturn(userId);
        String regId = "regId";

        Device device = new Device(user, regId, Device.Platform.android);
        when(deviceService.findById(deviceId)).thenReturn(Optional.of(device));
        when(securityService.isUnauthorized(userId)).thenReturn(false);

        controller.updateDeviceInfo(deviceId, regId);

        assertEquals(regId, device.regId);
    }

    @Test
    public void updateDeviceInfoReturnsAnOkResult() {
        long deviceId = 1;
        long userId = 2;
        User user = mock(User.class);
        when(user.userId).thenReturn(userId);
        Device device = new Device(user, "regId", Device.Platform.android);
        when(deviceService.findById(deviceId)).thenReturn(Optional.of(device));
        when(securityService.isUnauthorized(userId)).thenReturn(false);

        String newRegId = "newRegId";
        Result result = controller.updateDeviceInfo(deviceId, newRegId);

        assertEquals(OK, result.status());
    }


}
