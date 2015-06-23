package factories;

import com.google.common.base.Objects;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 6/22/15.
 */
public class IncludeEntity<T> {

    private Class<T> clazz;
    private int numToGenerate;
    private Map<String, Object> propertyOverrides;

    public IncludeEntity(Class<T> clazz) {
        this(clazz, 1);
    }

    public IncludeEntity(Class<T> clazz, int numToGenerate) {
        this(clazz, numToGenerate, new HashMap<>());
    }

    public IncludeEntity(Class<T> clazz, int numToGenerate, Map<String, Object> propertyOverrides) {
        this.clazz = clazz;
        this.numToGenerate = numToGenerate;
        this.propertyOverrides = propertyOverrides;
    }

    public Class<T> getEntityClass() {
        return clazz;
    }

    public int getNumToGenerate() {
        return numToGenerate;
    }

    public Map<String, Object> getPropertyOverrides() {
        return propertyOverrides;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("clazz", clazz)
                .add("numToGenerate", numToGenerate)
                .add("propertyOverrides", propertyOverrides)
                .toString();
    }
}
