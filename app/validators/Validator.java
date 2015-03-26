package validators;

public interface Validator<T> {

    abstract boolean isSupported(Object obj);
    abstract boolean isValid(T value);
    abstract String getErrorMessage();
}
