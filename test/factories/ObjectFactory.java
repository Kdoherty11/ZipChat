package factories;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import models.entities.*;
import play.Logger;
import play.data.validation.Constraints;
import play.db.jpa.JPA;
import utils.DbUtils;
import utils.TestUtils;

import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Defaults.defaultValue;

/**
 * Created by kevin on 6/15/15.
 */
public class ObjectFactory<T> {

    private static final Map<Class, FactoryDefaults> DEFAULT_VALUES = new HashMap<>();
    private static final Cache<Class, Field> idFieldNameCache = CacheBuilder.newBuilder()
            .maximumSize(30)
            .build();

    private static final Set<Class> REGISTERED_MODELS = ImmutableSet.of(
            AbstractRoom.class,
            AbstractUser.class,
            AnonUser.class,
            Device.class,
            Message.class,
            PrivateRoom.class,
            PublicRoom.class,
            Request.class,
            User.class
    );

    static {
        DEFAULT_VALUES.put(User.class, new UserDefaults());
        DEFAULT_VALUES.put(Device.class, new DeviceDefaults());
        DEFAULT_VALUES.put(Message.class, new MessageDefaults());
        DEFAULT_VALUES.put(PublicRoom.class, new PublicRoomDefaults());
        DEFAULT_VALUES.put(AnonUser.class, new AnonUserDefaults());
    }

    private Class<T> entityClass;
    private List<T> createdEntities = new ArrayList<>();
    private Map<Class, ObjectFactory<?>> createdFactories = new HashMap<>();
    private boolean didCleanUp = false;

    public ObjectFactory(Class<T> entityClass) {
        if (!Modifier.isAbstract(entityClass.getModifiers())) {
            this.entityClass = entityClass;
        } else {
            List<Class> subclasses = getSubclasses(entityClass);
            if (subclasses.isEmpty()) {
                throw new IllegalArgumentException("No subclasses have been registered for " + entityClass.getSimpleName());
            }

            this.entityClass = subclasses.get(new Random().nextInt(subclasses.size()));
        }
    }

    public static ObjectFactory of(Class<?> clazz) {
        return new ObjectFactory<>(clazz);
    }

    private static boolean isCollection(Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }

    private static boolean isList(Field field) {
        return List.class.isAssignableFrom(field.getType());
    }

    private static boolean isSet(Field field) {
        return Set.class.isAssignableFrom(field.getType());
    }

    private static boolean isIdField(Field field) {
        return field.getAnnotation(Id.class) != null;
    }

    private static boolean isRequiredField(Field field) {
        return field.getAnnotation(Constraints.Required.class) != null;
    }

    private static boolean fieldHasDefaultValue(Object entity, Field field) throws IllegalAccessException {
        Object defaultValue = defaultValue(field.getType());
        Object entityValue = field.get(entity);

        return defaultValue == null ? entityValue == null : defaultValue.equals(entityValue);
    }

    private static Optional<Field> getAssociatedField(Field field) {
        if (field.getAnnotation(OneToMany.class) != null) {
            return getAssociatedOneToManyField(field);
        }
        return Optional.empty();
    }

    private static Optional<Field> getAssociatedOneToManyField(Field field) {
        return Arrays.asList(getParameterizedType(field).getFields()).stream()
                .filter(associatedField -> associatedField.getAnnotation(ManyToOne.class) != null
                        && associatedField.getType() == field.getDeclaringClass())
                .findFirst();
    }

    private static List<Class> getSubclasses(Class<?> abstractClass) {
        List<Class> subclasses = new ArrayList<>();
        for (Class clazz : REGISTERED_MODELS) {
            if (abstractClass.isAssignableFrom(clazz) && abstractClass != clazz) {
                subclasses.add(clazz);
            }
        }
        return subclasses;
    }

    private static Class getParameterizedType(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            return (Class<?>) pType.getActualTypeArguments()[0];
        } else {
            throw new AssertionError(field.getName() + " does not have a generic type");
        }
    }

    public Set<T> createSet(int size) throws Throwable {
        return createSet(size, Collections.emptyMap());
    }

    public Set<T> createSet(int size, Map<String, Object> propertyOverrides) throws Throwable {
        Set<T> entities = new HashSet<>();

        for (int i = 0; i < size; i++) {
            entities.add(create(propertyOverrides));
        }

        return entities;
    }

    public List<T> createList(int size) throws Throwable {
        return createList(size, Collections.emptyMap());
    }

    public List<T> createList(int size, Map<String, Object> propertiesOverrides) throws Throwable {
        List<T> entities = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            entities.add(create(propertiesOverrides));
        }

        return entities;
    }

    public T create() throws Throwable {
        return create(Collections.emptyMap());
    }

    public T create(Map<String, Object> propertiesOverrides) throws Throwable {
        Map<String, Object> defaultProperties = DEFAULT_VALUES.getOrDefault(entityClass, new EmptyDefaults()).getDefaults();
        Map<Field, IncludeEntity> childEntityMap = new HashMap<>();

        T createdEntity = JPA.withTransaction(() -> {
                    T entity = entityClass.newInstance();

                    Field[] fields = entityClass.getFields();
                    for (Field field : fields) {
                        if (isIdField(field)) {
                            continue;
                        }

                        String fieldName = field.getName();
                        Class<?> fieldType = isCollection(field) ? getParameterizedType(field) : field.getType();
                        Object value = propertiesOverrides.get(fieldName);

                        if (value != null) {
                            if (value instanceof IncludeEntity) {
                                childEntityMap.put(field, (IncludeEntity) value);
                            } else {
                                field.set(entity, value);
                            }
                        } else if (fieldHasDefaultValue(entity, field)) {
                            if (defaultProperties != null && defaultProperties.containsKey(fieldName)) {
                                value = defaultProperties.get(fieldName);
                                field.set(entity, value);
                            } else if (REGISTERED_MODELS.contains(fieldType) && isRequiredField(field)) {
                                int count = isCollection(field) ? 2 : 1;
                                IncludeEntity includeEntity = new IncludeEntity<>(fieldType, count);
                                createChildEntity(field, entity, includeEntity);
                            }
                        }
                    }

                    JPA.withTransaction(() -> JPA.em().persist(entity));

                    createdEntities.add(entity);

                    return entity;
                }
        );

        createChildEntities(childEntityMap, createdEntity);

        return createdEntity;
    }

    private void createChildEntity(Field childField, T parentEntity, IncludeEntity includeEntity) throws Throwable {
        ObjectFactory factory = getOrCreateFactory(includeEntity.getEntityClass());
        Map<String, Object> includeObjectOverrides = includeEntity.getPropertyOverrides();
        Optional<Field> associatedFieldOptional = getAssociatedField(childField);
        if (associatedFieldOptional.isPresent()) {
            Field associatedField = associatedFieldOptional.get();
            if (isCollection(associatedField)) {
                if (isList(associatedField)) {
                    includeObjectOverrides.put(associatedField.getName(), Collections.singletonList(parentEntity));
                } else if (isSet(associatedField)) {
                    includeObjectOverrides.put(associatedField.getName(), TestUtils.setOf(parentEntity));
                } else {
                    throw new UnsupportedOperationException(associatedField.getType() + " is not supported");
                }
            } else {
                includeObjectOverrides.put(associatedField.getName(), parentEntity);
            }
        }

        Object value;
        if (isCollection(childField)) {
            if (isList(childField)) {
                value = factory.createList(includeEntity.getNumToGenerate(), includeObjectOverrides);
            } else if (isSet(childField)) {
                value = factory.createSet(includeEntity.getNumToGenerate(), includeObjectOverrides);
            } else {
                throw new UnsupportedOperationException(childField.getType() + " is not supported");
            }
        } else {
            value = factory.create(includeObjectOverrides);
        }

        Logger.debug("A: " + childField.getName());
        Logger.debug("B: " + value);

        childField.set(parentEntity, value);

        Logger.debug("C: " + parentEntity);
    }

    private void createChildEntities(Map<Field, IncludeEntity> includeEntityMap, T createdEntity) throws Throwable {
        for (Map.Entry<Field, IncludeEntity> entry : includeEntityMap.entrySet()) {
            createChildEntity(entry.getKey(), createdEntity, entry.getValue());
        }
    }

    private ObjectFactory getOrCreateFactory(Class<?> clazz) {
        ObjectFactory<?> factory = createdFactories.get(clazz);

        if (factory == null) {
            factory = new ObjectFactory<>(clazz);
            createdFactories.put(clazz, factory);
        }

        return factory;
    }

    private long getId(T entity) {
        try {
            return idFieldNameCache.get(entityClass, () -> Arrays.asList(entityClass.getFields())
                    .stream()
                    .filter(ObjectFactory::isIdField)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No ID for entity " + entity)))
                    .getLong(entity);
        } catch (ExecutionException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanUp() {
        JPA.withTransaction(() -> createdEntities.forEach(this::removeEntity));
        JPA.withTransaction(() -> createdFactories.values().forEach(ObjectFactory::cleanUp));

        didCleanUp = true;
    }

    private void removeEntity(T entity) {
        Optional<T> entityOptional = DbUtils.findEntityById(entityClass, getId(entity));
        if (entityOptional.isPresent()) {
            JPA.em().remove(entityOptional.get());
        }
    }

    @Override
    public void finalize() throws Throwable {
        try {
            if (!didCleanUp && !(createdEntities.isEmpty() && createdFactories.isEmpty())) {
                Logger.error("Cleanup not called for " + this);
                cleanUp();
            }
        } catch (Exception e) {
            Logger.error("Exception in ObjectFactory.finalize", e);
        } finally {
            super.finalize();
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("entityClass", entityClass)
                .toString();
    }

    //    private Optional<Field> getAssociatedOneToOneField(Field field) {
//        return Arrays.asList(field.getType().getFields()).stream()
//                .filter(associatedField -> associatedField.getAnnotation(OneToOne.class) != null
//                        && associatedField.getType() == field.getDeclaringClass())
//                .findFirst();
//    }

    //

    //
//    private Optional<Field> getAssociatedManyToOneField(Field field) {
//        return Arrays.asList(field.getType().getFields()).stream()
//                .filter(associatedField -> associatedField.getAnnotation(OneToMany.class) != null
//                        && getParameterizedType(associatedField) == field.getDeclaringClass())
//                .findFirst();
//    }
//
//    private Optional<Field> getAssociatedManyToManyField(Field field) {
//        return Arrays.asList(getFieldType(field).getFields()).stream()
//                .filter(associatedField -> associatedField.getAnnotation(ManyToMany.class) != null
//                        && associatedField.getGenericType() == field.getDeclaringClass())
//                .findFirst();
//    }
}
