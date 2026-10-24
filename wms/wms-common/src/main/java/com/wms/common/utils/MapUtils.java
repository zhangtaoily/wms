package com.wms.common.utils;

import lombok.experimental.UtilityClass;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Maps;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@UtilityClass
public class MapUtils {

    public static <K, V> boolean containsAnyKey(Map<K, V> map, Iterable<K> keys) {
        return ListUtils.adaptIterable(keys).anySatisfy(map::containsKey);
    }

    public static <K, V> List<V> getKeys(Map<K, V> map, Iterable<K> keys) {
        keys = ListUtils.adaptIterable(keys).distinct();
        if (ListUtils.isEmpty(keys)) {
            return ListUtils.empty();
        }

        return ListUtils.collect(keys, map::get);
    }

    public static <K, V> Map<K, V> of(K k, V v) {
        HashMap<K, V> kvHashMap = new HashMap<>();
        kvHashMap.put(k, v);
        return kvHashMap;
    }

    public static <K, V> Map<K, V> of(K k, V v, K k1, V v1) {
        Map<K, V> of = of(k, v);
        of.put(k1, v1);
        return of;
    }

    public static <K, V> Map<K, V> of(K k, V v, K k1, V v1, K k2, V v2) {
        Map<K, V> of = of(k, v, k1, v1);
        of.put(k2, v2);
        return of;
    }

    public static <K, V> Map<K, V> of(K k, V v, K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> of = of(k, v, k1, v1, k2, v2);
        of.put(k3, v3);
        return of;
    }

    public static <K, V> Map<K, V> of(K k, V v, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        Map<K, V> of = of(k, v, k1, v1, k2, v2, k3, v3);
        of.put(k4, v4);
        return of;
    }

    public static <K, V> Map<K, V> of(K k, V v, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        Map<K, V> of = of(k, v, k1, v1, k2, v2, k3, v3, k4, v4);
        of.put(k5, v5);
        return of;
    }

    public static <K, V> Map<K, V> of(K k, V v, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        Map<K, V> of = of(k, v, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        of.put(k6, v6);
        return of;
    }

    public static <K, V> Map<K, V> of(K k, V v, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        Map<K, V> of = of(k, v, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        of.put(k7, v7);
        return of;
    }

    public static <K, V> Map<K, V> of(K k, V v, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        Map<K, V> of = of(k, v, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        of.put(k8, v8);
        return of;
    }

    public static <K, V> Map<K, V> of(K k, V v, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        Map<K, V> of = of(k, v, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        of.put(k9, v9);
        return of;
    }

    public static <K, V> Map<K, V> of(K k, V v, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        Map<K, V> of = of(k, v, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        of.put(k10, v10);
        return of;
    }

    public static <K, V> Map<K, V> empty() {
        return new HashMap<>();
    }

    public static <K, V> LinkedHashMap<K, V> toLinkedHashMap(Map<K, V> map) {
        return new LinkedHashMap<>(map);
    }

    /**
     * 如果 Map 中的此Key，则执行consumer函数
     *
     * @param map       可为null，为null时，此方法直接返回 null
     * @param k         键
     * @param vConsumer v的消费
     */
    public static <K, V> void ifPresentConsumer(Map<K, V> map, K k, Consumer<V> vConsumer) {
        ifPresentApply(map, k, v -> {
            vConsumer.accept(v);
            return null;
        });
    }

    /**
     * 如果 Map 中的此Key，则执行 Function 函数，将 v 转换为指定类型
     *
     * @param map        可为null，为null时，此方法直接返回 null
     * @param k          键
     * @param vxFunction v的转换函数
     * @return 经过 vxFunction 转换后的值
     */
    public static <K, V, X> X ifPresentApply(Map<K, V> map, K k, Function<V, X> vxFunction) {
        if (ObjectUtil.isNullOrEmpty(map)) {
            return null;
        }

        return Maps.adapt(map).ifPresentApply(k, vxFunction::apply);
    }

    /**
     * 转换Map的值，注意，因为Key转换可能会有重复值，由于Key覆盖，导致最终生成的 Map 可能会小于原 Map
     *
     * @param map           原 Map
     * @param keyFunction   Key转换函数
     * @param valueFunction 值转换函数
     * @return 转换了键值的新 Map
     */
    public static <K, V, K1, V1> Map<K1, V1> transform(Map<K, V> map, Function<K, K1> keyFunction, Function<V, V1> valueFunction) {
        if (ObjectUtil.isNullOrEmpty(map)) {
            return empty();
        }

        Map<K1, V1> transformedMap = empty();
        map.forEach((k, v) -> transformedMap.put(keyFunction.apply(k), valueFunction.apply(v)));

        return transformedMap;
    }

    /**
     * 转换Map的值
     *
     * @param map           原 Map
     * @param valueFunction 值转换函数
     * @return 值被转换以后的新 MAP
     */
    public static <K, V, V1> Map<K, V1> transform(Map<K, V> map, Function<V, V1> valueFunction) {
        return transform(map, k -> k, valueFunction);
    }

    /**
     * 获取仅一个Map的第一个值，如果有多值，则随机获取一个，它可能返回 null
     */
    public static <K, V> V getFirstValue(Map<K, V> map) {
        K k = ListUtils.safeGet(ListUtils.adaptIterable(map.keySet()), 0);
        return map.get(k);
    }

    /**
     * 克隆一个 Map
     */
    public static <K, V> Map<K, V> clone(Map<K, V> map) {
        Map<K, V> empty = empty();
        if (ObjectUtil.nonNullOrEmpty(map)) {
            empty.putAll(map);
        }

        return empty;
    }

    /**
     * 将 Map 进行平铺为 元组
     */
    public static <K, V> MutableList<Tuple2<K, V>> toTuple2(Map<K, V> map) {
        if (map.isEmpty()) {
            return ListUtils.empty();
        }

        MutableList<Tuple2<K, V>> list = ListUtils.empty();
        map.forEach((k, v) -> list.add(Tuple2.of(k, v)));
        return list;
    }

    public static <K, V> Map<K, V> merge(Map<K, V> map1,Map<K, V> map2) {
        if (map1 == null) {
            map1 = MapUtils.empty();
        }
        if (ObjectUtil.nonNullOrEmpty(map2)) {
            map1.putAll(map2);
        }

        return map1;
    }

    /**
     * 合并两个Map，相同的key进行值累加
     * 注意：此方法只支持数值类型的值累加
     */
    public static <K, V extends Number> Map<K, V> mergeAdd(Map<K, V> map1,Map<K, V> map2) {
        if (map1 == null) {
            map1 = MapUtils.empty();
        }
        if (ObjectUtil.nonNullOrEmpty(map2)) {
            for (Map.Entry<K, V> entry : map2.entrySet()) {
                K key = entry.getKey();
                V value2 = entry.getValue();
                if (map1.containsKey(key)) {
                    // 存在相同key，累加值
                    V value1 = map1.get(key);
                    Number sum = null;
                    if (value1 instanceof Integer && value2 instanceof Integer) {
                        sum = value1.intValue() + value2.intValue();
                    } else if (value1 instanceof Long && value2 instanceof Long) {
                        sum = value1.longValue() + value2.longValue();
                    } else if (value1 instanceof Double && value2 instanceof Double) {
                        sum = value1.doubleValue() + value2.doubleValue();
                    } else if (value1 instanceof Float && value2 instanceof Float) {
                        sum = value1.floatValue() + value2.floatValue();
                    } else if (value1 instanceof BigDecimal && value2 instanceof BigDecimal) {
                        sum = ((BigDecimal) value1).add((BigDecimal) value2);
                    } else {
                        // 其他数值类型，转换为double累加
                        sum = value1.doubleValue() + value2.doubleValue();
                    }
                    map1.put(key, (V) sum);
                } else {
                    // 不存在相同key，直接添加
                    map1.put(key, value2);
                }
            }
        }

        return map1;
    }
}
