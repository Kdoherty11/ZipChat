package unit.models;

import factories.RequestFactory;
import models.entities.Request;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by kdoherty on 7/29/15.
 */
public class RequestTest {

    @Test
    public void setRequestId() {
        long requestId = 1;
        Request request = new Request();
        request.requestId = requestId;
        assertEquals(requestId, request.requestId);
    }

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

    @Test
    public void getIdNullRequest() {
        long id = Request.getId(null);
        assertEquals(-1, id);
    }

    @Test
    public void getIdNonNullRequest() {
        long expectedId = 1;
        Request request = new Request();
        request.requestId = expectedId;
        long actualId = Request.getId(request);
        assertEquals(expectedId, actualId);
    }

}
