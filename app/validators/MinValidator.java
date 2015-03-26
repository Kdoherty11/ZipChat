package validators;

class MinValidator implements Validator<Number> {

    private long min;

    public MinValidator(long value) {
        this.min = value;
    }

    @Override
    public Class<Number> getSupportedClass() {
        return Number.class;
    }

    @Override
    public boolean isValid(Number object) {
        return object == null || object.longValue() >= min;
    }

    @Override
    public String getErrorMessage() {
        return "must be at least " + min;
    }
}
