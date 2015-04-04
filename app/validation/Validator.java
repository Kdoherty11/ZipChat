package validation;

import java.util.Optional;

public interface Validator<T> {

    Class getAcceptedClass();
    boolean isValid(Optional<T> valueOptional);
    String getErrorMessage();
}
