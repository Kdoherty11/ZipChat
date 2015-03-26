package validators;

/**
 * Factory methods for Validators
 */
public class Validators {

    private Validators() {}

    public static Validator<Number> min(long value) {
        return new MinValidator(value);
    }

}
