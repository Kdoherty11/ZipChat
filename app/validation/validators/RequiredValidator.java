package validation.validators;

import validation.Validator;

import java.util.Optional;

public class RequiredValidator implements Validator<Object> {

    public static final String ERROR_MESSAGE = "This field is required";

    RequiredValidator() {}

    @Override
    public boolean isValid(Optional<Object> valueOptional) {
        return valueOptional.isPresent();
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
