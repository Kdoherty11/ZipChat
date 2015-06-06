package validation.validators;

import com.google.common.collect.Sets;
import validation.Validator;

import java.util.Arrays;
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

    public static Validator<String> enumValue(Class<? extends Enum> enumClass) {
        Enum[] enumValues = enumClass.getEnumConstants();
        Set<String> enumValueStrings = Arrays.stream(enumValues).map(Enum::name).collect(Collectors.toSet());
        return new WhiteListValidator<>(enumValueStrings);
    }

    @SafeVarargs
    public static <T> Validator<T> whiteList(T... whiteList) {
        return new WhiteListValidator<>(Sets.newHashSet(whiteList));
    }

}
