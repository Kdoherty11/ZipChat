package validators;

import java.util.*;

public class FieldValidator {

    private String fieldName;
    private Validator[] validators;
    private Object value;

    public FieldValidator(String fieldName, Object value, Validator... validators) {
        this.fieldName = fieldName;
        this.validators = validators;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getErrors() {
        Map<String, List<String>> errors = new HashMap<>();

        Arrays.asList(validators).forEach(validator -> {

            if (validator.isSupported(value) && !validator.isValid(value)) {
                List<String> validationErrors = errors.get(fieldName);

                if (validationErrors == null) {
                    validationErrors = new ArrayList<>();
                    errors.put(fieldName, validationErrors);
                }

                validationErrors.add(validator.getErrorMessage());
            }
        });

        return errors;
    }
}
