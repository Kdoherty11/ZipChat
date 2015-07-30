package unit.models;

import factories.MessageFactory;
import models.entities.Message;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by kdoherty on 7/29/15.
 */
public class MessageTest {

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
