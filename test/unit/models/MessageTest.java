package unit.models;

import factories.MessageFactory;
import models.Message;
import models.PublicRoom;
import models.User;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.*;

/**
 * Created by kdoherty on 7/29/15.
 */
public class MessageTest {

    @Test(expected = NullPointerException.class)
    public void constructorRoomCantBeNull() {
        new Message(null, new User(), "message");
    }

    @Test(expected = NullPointerException.class)
    public void constructorSenderCantBeNull() {
        new Message(new PublicRoom(), null, "message");
    }

    @Test(expected = NullPointerException.class)
    public void constructorMessageCantBeNull() {
        new Message(new PublicRoom(), new User(), null);
    }

    @Test
    public void constructorSetsRoomSenderAndMessage() {
        PublicRoom room = new PublicRoom();
        User sender = new User();
        String msgStr = "message";
        Message message = new Message(room, sender, msgStr);

        assertSame(room, message.room);
        assertSame(sender, message.sender);
        assertSame(msgStr, message.message);
    }

    @Test
    public void createdAtIsSetByDefault() {
        Message message = new Message();
        assertEquals(
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                message.createdAt,
                1000l);
    }

    @Test
    public void favoritesIsEmptyByDefault() {
        Message message = new Message();
        assertTrue(message.favorites.isEmpty());
    }

    @Test
    public void flagsIsEmptyByDefault() {
        Message message = new Message();
        assertTrue(message.flags.isEmpty());
    }

    @Test
    public void setMessageId() {
        // setMessageId(long messageId) method generated in byte code
        // so need to test for code coverage
        long messageId = 1;
        Message message = new Message();
        message.messageId = messageId;
        assertEquals(messageId, message.messageId);
    }

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Message.class)
                .suppress(Warning.STRICT_INHERITANCE) // Making equals/hashcode final messes up Mockito
                .verify();
    }

    @Test
    public void toStringAllNull() {
        Message message = new Message();
        String actual = message.toString();
        assertNotNull(actual);
    }

    @Test
    public void toStringNonNull() throws InstantiationException, IllegalAccessException {
        Message message = new MessageFactory().create(
                MessageFactory.Trait.WITH_ANON_SENDER,
                MessageFactory.Trait.WITH_PUBLIC_ROOM);
        String actual = message.toString();
        assertNotNull(actual);
    }

}
