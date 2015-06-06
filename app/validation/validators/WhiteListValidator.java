package validation.validators;

import validation.Validator;

import java.util.Optional;
import java.util.Set;

public class WhiteListValidator<T> implements Validator<T> {

    private Set<T> whiteList;

    WhiteListValidator(Set<T> whiteList) {
        this.whiteList = whiteList;
    }

    @Override
    public boolean isValid(Optional<T> valueOptional) {
        return !valueOptional.isPresent() || whiteList.contains(valueOptional.get());
    }

    @Override
    public String getErrorMessage() {
        return "Invalid value. Must be one of " + whiteList;
    }
}
