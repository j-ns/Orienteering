package com.jns.orienteering.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javafx.beans.property.BooleanProperty;

public class MapUtils {

    private static final double LOAD_FACTOR = 0.75;

    private MapUtils() {
    }

    public static <K, V> Map<K, V> createMap(Function<Integer, Map<K, V>> mapSupplier, int ensuredCapacity) {
        return mapSupplier.apply(calculateCapacity(ensuredCapacity));
    }

    public static <K, V> Map<K, V> createMap(int ensuredCapacity) {
        return new HashMap<>(calculateCapacity(ensuredCapacity));
    }

    public static int calculateCapacity(int ensuredCapacity) {
        return (int) (ensuredCapacity / LOAD_FACTOR + 1);

    }

    public static <K, T> Map<K, T> createMap(Collection<T> source, Function<T, K> keyMapper) {
        return createMap(source, keyMapper, t -> t);
    }

    public static <K, V, T> Map<K, V> createMap(Collection<T> source, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return createMap(HashMap::new, source, keyMapper, valueMapper);
    }

    public static <K, V, T> Map<K, V> createMap(Function<Integer, Map<K, V>> mapSupplier, Collection<T> source, Function<T, K> keyMapper,
                                                Function<T, V> valueMapper) {
        Map<K, V> map = createMap(mapSupplier, source.size());
        fillMap(map, source, keyMapper, valueMapper);
        return map;
    }

    public static <K, V, T> void fillMap(Map<K, V> target, Collection<T> source, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        for (T obj : source) {
            K key = keyMapper.apply(obj);
            V value = valueMapper.apply(obj);
            target.put(key, value);
        }
    }

    public static <K, V> V getOrCreate(Map<K, V> map, K key, Function<K, V> valueMapper) {
        V value = map.get(key);
        if (value == null) {
            value = valueMapper.apply(key);
            map.put(key, value);
        }
        return value;
    }

    public static <K, V> boolean computeIfPresent(Map<K, V> map, K key, UnaryOperator<V> mappingFunction) {
        V oldVal = map.get(key);
        if (oldVal != null) {
            V newVal = mappingFunction.apply(oldVal);
            map.put(key, newVal);
            return true;
        }
        return false;
    }

    public static void setBooleans(Map<?, BooleanProperty> map, boolean value) {
        for (BooleanProperty bool : map.values()) {
            bool.set(value);
        }
    }
}
