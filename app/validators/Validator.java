package validators;

public interface Validator<T> {

    abstract Class<T> getSupportedClass();
    abstract boolean isValid(T value);
    abstract String getErrorMessage();
}
