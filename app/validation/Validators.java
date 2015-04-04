package validation;

import validation.validators.MinValidator;
import validation.validators.RequiredValidator;
import validation.validators.WhiteListValidator;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Factory methods for Validators
 */
public class Validators {

    private Validators() {
    }

    public static Validator<Number> positive() {
        return new MinValidator(1);
    }

    public static Validator<Number> min(long value) {
        return new MinValidator(value);
    }

    public static Validator<Object> required() {
        return new RequiredValidator();
    }

    @SafeVarargs
    public static <T> WhiteListValidator whiteList(T... whiteList) {
        return new WhiteListValidator(new HashSet<>(Arrays.asList(whiteList)));
    }

}
