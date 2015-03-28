package validation;

public interface Validator<T> {

    boolean accepts(Object obj);
    boolean isValid(T value);
    String getErrorMessage();
}
