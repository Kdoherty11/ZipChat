package factories;

/**
 * Created by kdoherty on 7/5/15.
 */
public class PropOverride {

    private String fieldName;
    private Object fieldValue;

    private PropOverride(String fieldName, Object fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public static PropOverride of(String fieldName, Object fieldValue) {
        return new PropOverride(fieldName, fieldValue);
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
