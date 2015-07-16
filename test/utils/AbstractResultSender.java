package utils;

import play.mvc.Http;
import play.mvc.Result;

/**
 * Created by kdoherty on 7/8/15.
 */
public abstract class AbstractResultSender {

    private String restAction;
    protected String url;

    private boolean hasAddedParams = false;

    protected AbstractResultSender(String restAction, String url) {
        this.restAction = restAction;
        this.url = url;
    }

    protected abstract Result send();

    protected AbstractResultSender addQueryParam(String key, Object value) {
        if (!hasAddedParams) {
            url += "?";
            hasAddedParams = true;
        } else {
            url += "&";
        }

        url += (key + "=" + value);

        return this;
    }

    protected Http.RequestBuilder getRequestBuilder() {
        return new Http.RequestBuilder().uri(url).method(restAction);
    }
}
