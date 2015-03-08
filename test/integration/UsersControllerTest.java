package integration;

import models.Platform;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Result;

import java.util.*;

import static adapters.UsersControllerAdapter.*;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class UsersControllerTest extends AbstractControllerTest {

    @Test
    public void createUserSuccess() throws JSONException {
        Result createResult = createUser(Optional.empty(), Optional.empty());
        assertThat(status(createResult)).isEqualTo(OK);

        JSONObject createJson = new JSONObject(contentAsString(createResult));
        assertThat(createJson.getLong(ID_KEY)).isNotNull();
        assertThat(createJson.getString(NAME_KEY)).isEqualTo(NAME);
        assertThat(createJson.getString(FB_KEY)).isEqualTo(FB_ID);

        Result showResult = showUser(createJson.getLong(ID_KEY));
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJson = new JSONObject(contentAsString(showResult));
        assertThat(showJson.getLong(ID_KEY)).isNotNull();
    }

    @Test
    public void createUserNoName() {
        Result noNameResult = createUser(Optional.empty(), Optional.of(Arrays.asList(NAME_KEY)));
        assertThat(status(noNameResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createUserNoFbId() {
        Result noFbIdResult = createUser(Optional.empty(), Optional.of(Arrays.asList(FB_KEY)));
        assertThat(status(noFbIdResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createUserNoPlatform() {
        Result noPlatformResult = createUser(Optional.empty(), Optional.of(Arrays.asList(PLATFORM_KEY)));
        assertThat(status(noPlatformResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void createUserBadPlatform() {
        Map<String, String> badPlatformMap = new HashMap<>();
        badPlatformMap.put(PLATFORM_KEY, "platform");
        Result badPlatformResult = createUser(Optional.of(badPlatformMap), Optional.empty());
        assertThat(status(badPlatformResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void getUsersSuccess() throws JSONException {
        createUser(Optional.empty(), Optional.empty());

        Result allUsersResult = getUsers();
        assertThat(status(allUsersResult)).isEqualTo(OK);

        JSONArray allUsersJson = new JSONArray(contentAsString(allUsersResult));
        assertThat(allUsersJson.length()).isEqualTo(1);
    }

    @Test
    public void showUserSuccess() throws JSONException {
        long createdId = getCreateUserId(Optional.empty(), Optional.empty());

        Result showResult = showUser(createdId);
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJsonResult = new JSONObject(contentAsString(showResult));
        assertThat(showJsonResult.getLong(ID_KEY)).isEqualTo(createdId);
        assertThat(showJsonResult.getString(NAME_KEY)).isEqualTo(NAME);
        assertThat(showJsonResult.getString(FB_KEY)).isEqualTo(FB_ID);
        assertThat(showJsonResult.getString(REG_KEY)).isEqualTo(REG_ID);
        assertThat(showJsonResult.getString(PLATFORM_KEY)).isEqualTo(PLATFORM);
    }

    @Test
    public void showUserFailure() throws JSONException {
        Result showBadId = showUser(1);
        assertThat(status(showBadId)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void updateUserSuccess() throws JSONException {
        long createdId = getCreateUserId(Optional.empty(), Optional.empty());

        String updateUserId = "-1";
        String updateName = "Updated Name";
        String updateFbId = "9999999999";
        String updateRegId = "8888888888";
        String updatePlatform = "ios";

        Map<String, String> updateFields = new HashMap<>();
        updateFields.put(ID_KEY, updateUserId);
        updateFields.put(NAME_KEY, updateName);
        updateFields.put(FB_KEY, updateFbId);
        updateFields.put(REG_KEY, updateRegId);
        updateFields.put(PLATFORM_KEY, updatePlatform);

        Result updateResult = updateUser(createdId, updateFields);
        assertThat(status(updateResult)).isEqualTo(OK);

        JSONObject updateJson = new JSONObject(contentAsString(updateResult));
        assertThat(updateJson.getLong(ID_KEY)).isEqualTo(createdId);
        assertThat(updateJson.getString(NAME_KEY)).isEqualTo(updateName);
        assertThat(updateJson.getString(FB_KEY)).isEqualTo(updateFbId);
        assertThat(updateJson.getString(REG_KEY)).isEqualTo(updateRegId);
        assertThat(updateJson.getString(PLATFORM_KEY)).isEqualTo(updatePlatform);

        Result showResult = showUser(createdId);
        assertThat(status(showResult)).isEqualTo(OK);

        JSONObject showJson = new JSONObject(contentAsString(showResult));
        assertThat(showJson.getString(NAME_KEY)).isEqualTo(updateName);
        assertThat(showJson.getString(FB_KEY)).isEqualTo(updateFbId);
        assertThat(showJson.getString(REG_KEY)).isEqualTo(updateRegId);
        assertThat(showJson.getString(PLATFORM_KEY)).isEqualTo(updatePlatform);
    }

    @Test
    public void updateUserBadId() {
        Result updateBadId = updateUser(1, Collections.emptyMap());
        assertThat(status(updateBadId)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void deleteUserSuccess() throws JSONException {
        long createId = getCreateUserId(Optional.empty(), Optional.empty());

        Result deleteResult = deleteUser(createId);
        assertThat(status(deleteResult)).isEqualTo(OK);

        Result showResult = showUser(createId);
        assertThat(status(showResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void deleteUserBadId() {
        Result deleteBadId = deleteUser(1);
        assertThat(status(deleteBadId)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void sendAndroidNotification() throws JSONException {
        String androidRegId = "APA91bFusim6I9TJpcoq0PFkzxYXcgegKyQdTqd8dLx-IW2elgYGvTrzuXDpzj35mVWG3G_doP1qtfiQSkvGpg4III9qHVLIOu2X2UvksgIK18cs7JMBE3b_kbRHpR_7CR-pylvV8R7bMeRHWT3e0eXnDZd7rRg_yg";
        testSendNotificationByRegId(androidRegId, Platform.android.toString(), OK);
    }

    @Test
    @Ignore
    public void sendIosNotification() throws JSONException {
        String iosRegId = "a1559c63af6a6da908667946561be8795fae109e49ac7ec2e8b27e629b004aa4";
        testSendNotificationByRegId(iosRegId, Platform.ios.toString(), OK);
    }

    @Test
    public void sendNotificationNoRegId() throws JSONException {
        long createId = getCreateUserId(Optional.empty(), Optional.of(Arrays.asList(REG_KEY)));

        Result notifyResult = sendNotification(createId, Collections.emptyMap());
        assertThat(status(notifyResult)).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void sendNotificationBadId() {
        Result notifyBadId = sendNotification(1, Collections.emptyMap());
        assertThat(status(notifyBadId)).isEqualTo(BAD_REQUEST);
    }

    @Test
    @Ignore
    public void sendNotificationBadPlatform() throws JSONException {
        String androidRegId = "APA91bFusim6I9TJpcoq0PFkzxYXcgegKyQdTqd8dLx-IW2elgYGvTrzuXDpzj35mVWG3G_doP1qtfiQSkvGpg4III9qHVLIOu2X2UvksgIK18cs7JMBE3b_kbRHpR_7CR-pylvV8R7bMeRHWT3e0eXnDZd7rRg_yg";
        testSendNotificationByRegId(androidRegId, "Platform", BAD_REQUEST);
    }

    @Test
    @Ignore
    public void sendAndroidNotificationBadRegId() throws JSONException {
        testSendNotificationByRegId("Bad Reg Id", Platform.android.toString(), BAD_REQUEST);
    }

    @Test
    @Ignore
    public void sendIosNotificationBadRegId() throws JSONException {
        testSendNotificationByRegId("Bad Reg Id", Platform.ios.toString(), BAD_REQUEST);
    }

    public void testSendNotificationByRegId(String regId, String platform, int status) throws JSONException {
        Map<String, String> formData = new HashMap<>();
        formData.put(REG_KEY, regId);
        formData.put(PLATFORM, platform);
        long createId = getCreateUserId(Optional.of(formData), Optional.empty());

        Result notifyResult = sendNotification(createId, Collections.emptyMap());
        assertThat(status(notifyResult)).isEqualTo(status);
    }
}
