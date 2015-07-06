package factories;

import com.github.javafaker.Faker;
import com.google.common.base.Preconditions;
import play.Logger;

import java.lang.reflect.Field;
import java.util.*;

import static com.google.common.base.Defaults.defaultValue;

/**
 * Created by kdoherty on 7/5/15.
 */
public abstract class GenericFactory<T> implements Factory<T> {

    private Class<T> entityClass;
    protected final Faker faker = new Faker();

    public GenericFactory(Class<T> entityClass) {
        this.entityClass = Preconditions.checkNotNull(entityClass);
    }

    Map<String, Object> getDefaultProperties() {
        return Collections.emptyMap();
    }

    @Override
    public T create(PropOverride... propOverrides) throws IllegalAccessException, InstantiationException {
        Map<String, Object> defaultProperties = getDefaultProperties();
        Logger.debug("Got default properties: " + defaultProperties.toString());
        Map<String, Object> propOverridesMap = getPropOverridesMap(propOverrides);

        T createdObj = entityClass.newInstance();
        Field[] fields = entityClass.getFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            if (propOverridesMap.containsKey(fieldName)) {
                field.set(createdObj, propOverridesMap.get(fieldName));
            } else if (fieldHasDefaultValue(createdObj, field) && defaultProperties.containsKey(fieldName)) {
                field.set(createdObj, defaultProperties.get(fieldName));
            }
        }

        return createdObj;
    }

    @Override
    public List<T> createList(int size, PropOverride... propOverrides) throws InstantiationException, IllegalAccessException {
        List<T> createdObjList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            createdObjList.add(create(propOverrides));
        }

        return createdObjList;
    }

    @Override
    public Set<T> createSet(int size, PropOverride... propOverrides) throws InstantiationException, IllegalAccessException {
        Set<T> createdObjSet = new HashSet<>();

        for (int i = 0; i < size; i++) {
            createdObjSet.add(create(propOverrides));
        }

        return createdObjSet;
    }

    private static Map<String, Object> getPropOverridesMap(PropOverride... propOverrides) {
        Map<String, Object> propOverrideMap = new HashMap<>();

        for (PropOverride propOverride : propOverrides) {
            if (propOverrideMap.containsKey(propOverride.getFieldName())) {
                throw new IllegalArgumentException("Conflicting property overrides");
            }

            propOverrideMap.put(propOverride.getFieldName(), propOverride.getFieldValue());
        }

        return propOverrideMap;
    }

    private static boolean fieldHasDefaultValue(Object entity, Field field) throws IllegalAccessException {
        Object defaultValue = defaultValue(field.getType());
        Object entityValue = field.get(entity);

        return defaultValue == null ? entityValue == null : defaultValue.equals(entityValue);
    }

}
