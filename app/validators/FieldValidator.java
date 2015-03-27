package validators;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Arrays;

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
    public Multimap<String, String> getErrors() {
        Multimap<String, String> errors = HashMultimap.create();

        Arrays.asList(validators).stream()
                .filter(validator -> validator.accepts(value) && !validator.isValid(value))
                .forEach(validator -> errors.put(fieldName, validator.getErrorMessage()));

        return errors;
    }
}
