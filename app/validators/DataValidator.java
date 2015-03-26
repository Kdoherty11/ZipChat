package validators;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.libs.Json.toJson;

public class DataValidator {

    private FieldValidator[] validators;
    private boolean validated = false;
    private Map<String, List<String>> errors = new HashMap<>();

    public DataValidator(FieldValidator... validators) {
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
        return toJson(errors);
    }
}
