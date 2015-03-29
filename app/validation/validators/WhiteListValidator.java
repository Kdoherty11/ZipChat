package validation.validators;

import validation.Validator;

import java.util.Set;

public class WhiteListValidator implements Validator<String> {

    private Set<String> whiteList;

    public WhiteListValidator(Set<String> whiteList) {
        this.whiteList = whiteList;
    }

    @Override
    public boolean accepts(Object obj) {
        return String.class.isInstance(obj);
    }

    @Override
    public boolean isValid(String value) {
        return whiteList.contains(value);
    }

    @Override
    public String getErrorMessage() {
        return "Invalid value. Must be one of " + whiteList;
    }
}
