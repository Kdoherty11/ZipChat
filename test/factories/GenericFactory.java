package factories;

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;
import java.util.*;

import static com.google.common.base.Defaults.defaultValue;

/**
 * Created by kdoherty on 7/5/15.
 */
public abstract class GenericFactory<T> implements Factory<T> {

    private Class<T> entityClass;

    public GenericFactory(Class<T> entityClass) {
        this.entityClass = Preconditions.checkNotNull(entityClass);
    }

    // Override this
    Map<String, Object> getDefaultProperties() {
        return Collections.emptyMap();
    }

    @SafeVarargs
    @Override
    public final T create(ObjectMutator<T>... mutators) throws IllegalAccessException, InstantiationException {
        Map<String, Object> defaultProperties = getDefaultProperties();
        Set<String> overriddenFields = new HashSet<>();

        T createdObj = entityClass.newInstance();

        for (ObjectMutator<T> mutator : mutators) {
            mutator.apply(createdObj);

            if (mutator instanceof PropOverride) {
                overriddenFields.add(((PropOverride) mutator).getFieldName());
            }
        }

        Field[] fields = entityClass.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (!overriddenFields.contains(fieldName) &&
                    fieldHasDefaultValue(createdObj, field)
                    && defaultProperties.containsKey(fieldName)) {
                field.set(createdObj, defaultProperties.get(fieldName));
            }
        }

        return createdObj;
    }

    @SafeVarargs
    @Override
    public final List<T> createList(int size, ObjectMutator<T>... mutators) throws
            InstantiationException, IllegalAccessException {
        List<T> createdObjList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            createdObjList.add(create(mutators));
        }

        return createdObjList;
    }

    @SafeVarargs
    @Override
    public final Set<T> createSet(int size, ObjectMutator<T>... mutators) throws
            InstantiationException, IllegalAccessException {
        Set<T> createdObjSet = new HashSet<>();

        for (int i = 0; i < size; i++) {
            createdObjSet.add(create(mutators));
        }

        return createdObjSet;
    }

    private static boolean fieldHasDefaultValue(Object entity, Field field) throws IllegalAccessException {
        Object defaultValue = defaultValue(field.getType());
        Object entityValue = field.get(entity);

        return defaultValue == null ? entityValue == null : defaultValue.equals(entityValue);
    }

}
