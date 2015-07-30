package unit.models;

import models.entities.Request;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

/**
 * Created by kdoherty on 7/29/15.
 */
public class RequestTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(Request.class)
                .suppress(Warning.STRICT_INHERITANCE) // Making equals/hashcode final messes up Mockito
                .verify();
    }
}
