package utils;

import play.mvc.Result;
import play.test.FakeRequest;

/**
 * Created by kdoherty on 7/8/15.
 */
public abstract class AbstractResultBuilder {

    protected String restAction;
    protected String url;

    protected AbstractResultBuilder(String restAction, String url) {
        this.restAction = restAction;
        this.url = url;
    }

    public abstract Result build();

    public FakeRequest buildFakeRequest() {
        return new FakeRequest(restAction, url);
    }
}
