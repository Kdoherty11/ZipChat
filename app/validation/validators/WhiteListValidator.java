package validation.validators;

import validation.Validator;

import java.util.Optional;
import java.util.Set;

public class WhiteListValidator implements Validator<Object> {

    private Set<Object> whiteList;

    public WhiteListValidator(Set<Object> whiteList) {
        this.whiteList = whiteList;
    }

    @Override
    public boolean isValid(Optional<Object> valueOptional) {
        return !valueOptional.isPresent() || whiteList.contains(valueOptional.get());
    }

    @Override
    public String getErrorMessage() {
        return "Invalid value. Must be one of " + whiteList;
    }
}
