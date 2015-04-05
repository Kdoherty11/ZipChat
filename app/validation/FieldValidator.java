package validation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import controllers.BaseController;
import play.mvc.Result;

import java.util.Optional;

public class FieldValidator<T> {

    private String fieldName;
    private Validator<? super T>[] validators;
    private T value;

    @SafeVarargs
    public FieldValidator(String fieldName, T value, Validator<? super T>... validators) {
        this.fieldName = fieldName;
        this.validators = validators;
        this.value = value;
    }

    public Multimap<String, String> getErrors() {
        Multimap<String, String> errors = HashMultimap.create();

        for (Validator<? super T> validator : validators) {
            if (!validator.isValid(Optional.ofNullable(value))) {
                errors.put(fieldName, validator.getErrorMessage());
            }
        }

        return errors;
    }

    public static Result typeError(String fieldName, Class<?> expectedType) {
        return BaseController.badRequestJson(ImmutableMap.of(fieldName, "Expected type " + expectedType.getSimpleName()));
    }
}
