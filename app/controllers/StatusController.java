package controllers;

import play.mvc.Result;

/**
 * Created by kdoherty on 7/8/15.
 */
public class StatusController extends BaseController {

    public Result status() {
        return OK_RESULT;
    }

}
