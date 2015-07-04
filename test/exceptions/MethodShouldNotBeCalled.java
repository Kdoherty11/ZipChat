package exceptions;

/**
 * Created by kdoherty on 7/2/15.
 */
public class MethodShouldNotBeCalled extends Exception {

    public MethodShouldNotBeCalled(String message) {
        super(message);
    }

    public MethodShouldNotBeCalled(String message, Throwable throwable) {
        super(message, throwable);
    }

}
