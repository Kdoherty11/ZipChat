package factories;

/**
 * Created by kdoherty on 7/5/15.
 */
public class FieldOverride<T> implements ObjectMutator<T> {

    private String fieldName;
    private Object fieldValue;

    private FieldOverride(String fieldName, Object fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public static FieldOverride of(String fieldName, Object fieldValue) {
        return new FieldOverride(fieldName, fieldValue);
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    @Override
    public void apply(T t) {
        try {
            t.getClass().getField(fieldName).set(t, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
