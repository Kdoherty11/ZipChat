package notifications;

import com.google.common.collect.ImmutableMap;
import models.entities.*;
import notifications.senders.AndroidNotificationSender;
import notifications.senders.IosNotificationSender;

import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 6/11/15.
 */
public abstract class AbstractNotification {

    protected static class Key {
        protected static final String EVENT = "event";
        protected static final String FACEBOOK_NAME = "name";
        protected static final String CHAT_REQUEST_RESPONSE = "response";
        protected static final String FACEBOOK_ID = "facebookId";
        protected static final String MESSAGE = "message";
        protected static final String ROOM_NAME = "roomName";
        protected static final String ROOM_ID = "roomId";
        protected static final String ROOM_TYPE = "roomType";
        protected static final String ROOM_RADIUS = "roomRadius";
        protected static final String ROOM_LATITUDE = "roomLatitude";
        protected static final String ROOM_LONGITUDE = "roomLongitude";
    }

    protected static class Event {
        protected static final String CHAT_REQUEST = "Chat Request";
        protected static final String CHAT_REQUEST_RESPONSE = "Chat Request Response";
        protected static final String CHAT_MESSAGE = "Chat Message";
        protected static final String MESSAGE_FAVORITED = "Message Favorited";
    }

    protected static class Value {
        protected static final String protected_ROOM_TYPE = "protectedRoom";
        protected static final String PUBLIC_ROOM_TYPE = "PublicRoom";
    }

    private Map<String, String> content;

    public AbstractNotification(String event, Map<String, String> content) {
        content.put(Key.EVENT, event);
        this.content = content;
    }

    public void send(List<String> androidRegIds, List<String> iosRegIds) {
        int numAndroidRegIds = androidRegIds.size();
        if (numAndroidRegIds == 1) {
            AndroidNotificationSender.INSTANCE.sendNotification(androidRegIds.get(0), content);
        } else if (numAndroidRegIds > 1) {
            AndroidNotificationSender.INSTANCE.sendBatchNotification(androidRegIds, content);
        }

        int numIosRegIds = iosRegIds.size();
        if (numIosRegIds == 1) {
            IosNotificationSender.INSTANCE.sendNotification(iosRegIds.get(0), content);
        } else if (numIosRegIds > 1) {
            IosNotificationSender.INSTANCE.sendBatchNotification(iosRegIds, content);
        }
    }

    protected static Map<String, String> getRoomData(AbstractRoom room) {
        if (room instanceof PublicRoom) {
            return getRoomData((PublicRoom) room);
        } else {
            return getRoomData((PrivateRoom) room);
        }
    }

    protected static Map<String, String> getRoomData(PublicRoom publicRoom) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.ROOM_TYPE, Value.PUBLIC_ROOM_TYPE)
                .put(Key.ROOM_ID, String.valueOf(publicRoom.roomId))
                .put(Key.ROOM_NAME, publicRoom.name)
                .put(Key.ROOM_RADIUS, String.valueOf(publicRoom.radius))
                .put(Key.ROOM_LATITUDE, String.valueOf(publicRoom.latitude))
                .put(Key.ROOM_LONGITUDE, String.valueOf(publicRoom.longitude))
                .build();
    }

    protected static Map<String, String> getRoomData(PrivateRoom protectedRoom) {
        return new ImmutableMap.Builder<String, String>()
                .put(Key.ROOM_TYPE, Value.protected_ROOM_TYPE)
                .put(Key.ROOM_ID, String.valueOf(protectedRoom.roomId)).build();
    }
}
