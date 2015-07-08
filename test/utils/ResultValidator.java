package utils;

import org.json.JSONException;
import org.json.JSONObject;
import play.Logger;
import play.mvc.Result;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.CREATED;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.status;

/**
 * Created by kdoherty on 7/8/15.
 */
public class ResultValidator {

    public static void validateCreateResult(Result createResult, String idKey) throws JSONException {
        assertThat(status(createResult)).isEqualTo(CREATED);
        JSONObject jsonResponse = new JSONObject(contentAsString(createResult));
        Logger.error("Create json: " + jsonResponse);
        JsonValidator.validateCreateJson(jsonResponse, idKey);
    }
}
