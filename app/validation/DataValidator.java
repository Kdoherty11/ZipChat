package validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Arrays;

import static play.libs.Json.toJson;

public class DataValidator {

    private FieldValidator<?>[] validators;
    private boolean validated = false;
    private Multimap<String, String> errors = HashMultimap.create();

    public DataValidator(FieldValidator<?>... validators) {
        this.validators = validators;
    }

    public boolean hasErrors() {
        if (!validated) {
            validate();
        }
        return !errors.isEmpty();
    }

    private void validate() {
        Arrays.asList(validators).forEach(validator -> errors.putAll(validator.getErrors()));
        validated = true;
    }

    public JsonNode errorsAsJson() {
        if (!validated) {
            throw new IllegalStateException("hasErrors must be called before getting the errors");
        }
        return toJson(errors.asMap());
    }
}
