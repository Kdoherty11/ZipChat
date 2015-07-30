package unit.models;

import factories.RequestFactory;
import models.entities.Request;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

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

    @Test
    public void toStringAllNull() {
        Request request = new Request();
        String actual = request.toString();
        assertNotNull(actual);
    }

    @Test
    public void toStringNonNull() throws InstantiationException, IllegalAccessException {
        Request request = new RequestFactory().create(RequestFactory.Trait.WITH_SENDER_AND_RECEIVER);
        String actual = request.toString();
        assertNotNull(actual);
    }
}
