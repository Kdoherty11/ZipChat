package unit.controllers;

import controllers.BaseController;
import controllers.StatusController;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Result;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.start;

/**
 * Created by kdoherty on 7/28/15.
 */
public class StatusControllerTest {

    private StatusController controller;

    @Before
    public void setUp() {
        controller = new StatusController();
        start(fakeApplication());
    }

    @Test
    public void statusReturnsOkResult() {
        Result result = controller.status();

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(BaseController.OK_STRING));
    }
}
