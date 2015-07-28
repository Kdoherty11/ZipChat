package validation.validators;

import com.google.common.primitives.Longs;
import validation.Validator;

import java.util.Optional;

/**
 * Created by kdoherty on 7/28/15.
 */
public class StringToLongValidator implements Validator<String> {

    public static final String ERROR_MESSAGE = "Expected type Long";

    StringToLongValidator() {}

    @Override
    public boolean isValid(Optional<String> valueOptional) {
        return !valueOptional.isPresent() || Longs.tryParse(valueOptional.get()) != null;
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
