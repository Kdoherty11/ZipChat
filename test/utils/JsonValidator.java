package utils;

import models.entities.PublicRoom;
import org.json.JSONException;
import org.json.JSONObject;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by kdoherty on 7/2/15.
 */
public class JsonValidator {

    public static void validateUserJson(JSONObject userJson) {
        try {
            final long userId = userJson.getLong("userId");
            final String facebookId = userJson.getString("facebookId");
            final String name = userJson.getString("name");

            assertThat(userId).isPositive();
            assertThat(facebookId).isNotNull();
            assertThat(facebookId).isNotEmpty();
            assertThat(name).isNotNull();
            assertThat(name).isNotEmpty();
        } catch (JSONException e) {
            throw new RuntimeException("Problem parsing user json", e);
        }
    }

    public static void validatePrivateRoom(JSONObject privateRoomJson) {
        try {
            final long roomId = privateRoomJson.getLong("roomId");
            final JSONObject sender = privateRoomJson.getJSONObject("sender");
            final JSONObject receiver = privateRoomJson.getJSONObject("receiver");

            final long lastActivity = privateRoomJson.getLong("lastActivity");

            assertThat(roomId).isPositive();
            validateUserJson(sender);
            validateUserJson(receiver);
            assertThat(lastActivity).isPositive();
        } catch (JSONException e) {
            throw new RuntimeException("Problem parsing public room json", e);
        }
    }

    public static void validatePublicRoom(JSONObject publicRoomJson) {

            PublicRoom publicRoom = parsePublicRoom(publicRoomJson);

            assertThat(publicRoom.roomId).isPositive();
            assertThat(publicRoom.name).isNotEmpty();
            assertThat(publicRoom.latitude).isGreaterThanOrEqualTo(-90.0).isLessThanOrEqualTo(90.0);
            assertThat(publicRoom.longitude).isGreaterThanOrEqualTo(-180.0).isLessThanOrEqualTo(180.0);
            assertThat(publicRoom.radius).isPositive();
            assertThat(publicRoom.lastActivity).isPositive();
    }

    // Not using Gson because we want the test to fail if a variable name changes
    public static PublicRoom parsePublicRoom(JSONObject publicRoomJson) {
        PublicRoom publicRoom = new PublicRoom();
        try {
            final long roomId = publicRoomJson.getLong("roomId");
            final String name = publicRoomJson.getString("name");
            final double latitude = publicRoomJson.getDouble("latitude");
            final double longitude = publicRoomJson.getDouble("longitude");
            final int radius = publicRoomJson.getInt("radius");
            final long lastActivity = publicRoomJson.getLong("lastActivity");

            publicRoom.roomId = roomId;
            publicRoom.name = name;
            publicRoom.latitude = latitude;
            publicRoom.longitude = longitude;
            publicRoom.radius = radius;
            publicRoom.lastActivity = lastActivity;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return publicRoom;
    }

    public static void validateMessageJson(JSONObject messageJson) throws JSONException {
        final long messageId = messageJson.getLong("messageId");
        final JSONObject senderJson = messageJson.getJSONObject("sender");
        final String message = messageJson.getString("message");
        final long createdAt = messageJson.getLong("createdAt");

        assertThat(messageId).isPositive();
        validateUserJson(senderJson);
        assertThat(message).isNotNull();
        assertThat(message).isNotEmpty();
        assertThat(createdAt).isPositive();
    }

    public static void validateCreateJson(JSONObject createJson, String idKey) throws JSONException {
        assertThat(createJson.getLong(idKey)).isPositive();
    }


}
