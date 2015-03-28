package validation.validators;

import validation.Validator;

public class MinValidator implements Validator<Number> {

    private long min;

    public MinValidator(long value) {
        this.min = value;
    }

    @Override
    public boolean accepts(Object obj) {
        return Number.class.isInstance(obj);
    }

    @Override
    public boolean isValid(Number object) {
        return object == null || object.longValue() >= min;
    }

    @Override
    public String getErrorMessage() {
        return "This field's value must be at least " + min;
    }
}
