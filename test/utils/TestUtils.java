package utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

}
