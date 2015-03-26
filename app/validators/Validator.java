package validators;

public interface Validator<T> {

    abstract boolean accepts(Object obj);
    abstract boolean isValid(T value);
    abstract String getErrorMessage();
}
