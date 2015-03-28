package validation.validators;

import validation.Validator;

public class RequiredValidator implements Validator<Object> {

    @Override
    public boolean accepts(Object obj) {
        return true;
    }

    @Override
    public boolean isValid(Object value) {
        return value != null;
    }

    @Override
    public String getErrorMessage() {
        return "This field is required";
    }
}
