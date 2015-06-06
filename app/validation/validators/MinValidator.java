package validation.validators;

import validation.Validator;

import java.util.Optional;

public class MinValidator implements Validator<Number> {

    private long min;

    MinValidator(long value) {
        this.min = value;
    }

    @Override
    public boolean isValid(Optional<Number> numOptional) {
        return !numOptional.isPresent() || numOptional.get().longValue() >= min;
    }

    @Override
    public String getErrorMessage() {
        return "This field's value must be at least " + min;
    }
}
