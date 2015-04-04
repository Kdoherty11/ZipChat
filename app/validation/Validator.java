package validation;

import java.util.Optional;

public interface Validator<T> {

    boolean isValid(Optional<T> valueOptional);
    String getErrorMessage();
}
