package validation.validators;

import validation.Validator;

import java.util.Optional;

public class RequiredValidator implements Validator<Object> {

    @Override
    public Class getAcceptedClass() {
        return Object.class;
    }

    @Override
    public boolean isValid(Optional<Object> valueOptional) {
        return valueOptional.isPresent();
    }

    @Override
    public String getErrorMessage() {
        return "This field is required";
    }
}
