package unit.models;

import models.entities.Message;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

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

}
