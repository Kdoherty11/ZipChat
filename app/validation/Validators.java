package validation;

import validation.validators.MinValidator;
import validation.validators.RequiredValidator;
import validation.validators.WhiteListValidator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Factory methods for Validators
 */
public class Validators {

    private Validators() {
    }

    public static Validator<Number> positive() {
        return new MinValidator(1);
    }

    public static Validator<Number> min(long value) {
        return new MinValidator(value);
    }

    public static Validator<Object> required() {
        return new RequiredValidator();
    }

    public static Validator<String> whiteList(Class<? extends Enum> enumClass) {
        return whiteList(Arrays.asList(enumClass.getEnumConstants()).stream()
                .map(Enum::name)
                .collect(Collectors.toSet()));
    }

    public static Validator<String> whiteList(String... whiteList) {
        return new WhiteListValidator(new HashSet<>(Arrays.asList(whiteList)));
    }

    public static Validator<String> whiteList(Set<String> whiteList) {
        return new WhiteListValidator(whiteList);
    }

}
