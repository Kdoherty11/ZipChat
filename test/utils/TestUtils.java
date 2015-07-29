package utils;

import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.Mode;
import play.inject.guice.GuiceApplicationLoader;

import javax.persistence.Id;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class TestUtils {

    public static String withQuotes(String string) {
        return "\"" + string + "\"";
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> map = mapOf(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> map = mapOf(k1, v1, k2, v2);
        map.put(k3, v3);
        return map;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3,
                                         V v3, K k4, V v4) {
        Map<K, V> map = mapOf(k1, v1, k2, v2, k3, v3);
        map.put(k4, v4);
        return map;
    }

    public static <E> Set<E> setOf(E element) {
        Set<E> set = new HashSet<>();
        set.add(element);
        return set;
    }

    public static long getId(Object object) {
        try {
            return Arrays.asList(object.getClass().getFields())
                    .stream()
                    .filter(field -> field.isAnnotationPresent(Id.class))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No ID for entity " + object))
                    .getLong(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    // Returns an Id that is not used by any of the input entities
    @SafeVarargs
    public static <T> long getUniqueId(T... entities) {
        Set<Long> objectIds = Arrays.asList(entities).stream().map(TestUtils::getId).collect(Collectors.toSet());
        for (long i = 0; i < entities.length + 1; i++) {
            if (!objectIds.contains(i)) {
                return i;
            }
        }
        throw new AssertionError("This method is broken...");
    }

    public static Object getPrivateStaticField(Class clazz, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(fieldName); //NoSuchFieldException
        f.setAccessible(true);
        return f.get(null);
    }
}
