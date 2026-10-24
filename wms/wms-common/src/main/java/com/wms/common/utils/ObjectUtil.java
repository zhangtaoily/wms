package com.wms.common.utils;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.BooleanUtils;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 对象工具类，这个类的所有空判断，都视 “ ” 空串 == null
 */
public class ObjectUtil extends org.springframework.util.ObjectUtils {

    /**
     * 判断一个对象是否为null或empty对象
     * empty对象包括空字符串(包括空格串)，空数组,字集合等
     */
    @SuppressWarnings("all")
    public static <T> boolean isNullOrEmpty(T t) {
        if (t == null) {
            return true;
        }

        if (t instanceof EmptyAble) {
            return ((EmptyAble) t).isEmpty();
        }

        if (t instanceof Page) {
            return ListUtils.isEmpty(((Page<?>) t).getRecords());
        }

        if (t.getClass().isArray()) {
            return Array.getLength(t) == 0;
        }

        if (t instanceof Collection) {
            return ((Collection<?>) t).isEmpty();
        }

        if (t instanceof Map) {
            return ((Map<?, ?>) t).isEmpty();
        }

        if (t instanceof CharSequence) {
            CharSequence cs = (CharSequence) t;
            for (int i = 0; i < cs.length(); i++) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
            return true;
        }

        if (t instanceof Optional) {
            return !((Optional<?>) t).isPresent();
        }

        //默认为false,即非null
        return false;
    }

    /**
     * 判断为空，或空串
     *
     * @param t   参数
     * @param <T> 类型
     * @return 是否为空
     */
    public static <T> boolean nonNullOrEmpty(T t) {
        return !isNullOrEmpty(t);
    }

    /**
     * Null Value Else，如果空对象（包括空串）的话，循环判断后续的
     * <p>
     * 如：nvl(null, "", 3) == 3
     */
    @SafeVarargs
    public static <T> T nvl(T t, T... defaults) {
        if (nonNullOrEmpty(t)) {
            return t;
        }

        return ListUtils.select(defaults, ObjectUtil::nonNullOrEmpty).getFirst(); // 返回第一个不为空的值
    }

    /**
     * 优化使用此方法，因为nvl会导致后面的表达式被执行，supplier 不会
     *
     * @param t          对象
     * @param tSuppliers 系列对象的获取函数
     * @return 第一个不为空的值
     */
    @SafeVarargs
    public static <T> T nvl2(T t, Supplier<T>... tSuppliers) {
        if (nonNullOrEmpty(t)) {
            return t;
        }

        if (ListUtils.adaptItems(tSuppliers).isEmpty()) {
            return null;
        }

        for (Supplier<T> supplier : tSuppliers) {
            T t1 = supplier.get();
            if (t1 != null) {
                return t1;
            }
        }

        return null;
    }

    /**
     * 如果为空，则取默认值
     */
    public static <T> T ifNull(T value, T defaultValue) {
        return nvl2(value, () -> defaultValue);
    }

    /**
     * 所有的都是空对象
     */
    public static boolean allNull(Object... objects) {
        //如果本身是空数组，也认为它包含了null
        if (objects == null || objects.length == 0) {
            return true;
        }

        return ListUtils.adaptArray(objects).allSatisfy(ObjectUtil::isNullOrEmpty);
    }

    /**
     * 所有对象都不为空
     */
    public static boolean allNoneNull(Object... objects) {
        //如果本身是个空数组，不符合：所有对象不为空的语义
        if (allNull(objects)) {
            return false;
        }

        return ListUtils.adaptArray(objects)
                .allSatisfy(ObjectUtil::nonNullOrEmpty);
    }

    /**
     * 递进式判断，对象不为空，且对象的方法不为空
     */
    @SafeVarargs
    public static <T, R> boolean allNoneNull(T object, Function<T, R>... functions) {
        //如果本身是个空数组，不符合：所有对象不为空的语义
        if (object == null) {
            return false;
        }

        return ListUtils
                .collect(ListUtils.adaptArray(functions), x -> nonNullOrEmpty(x.apply(object)))
                .allSatisfy(BooleanUtils::isTrue);
    }

    public static boolean anyNull(Object... objects) {
        return containsNull(objects);
    }

    /**
     * 有递进关系的判断
     * <p>
     * x == null || x.getX() == null
     */
    @SafeVarargs
    public static <E> boolean anyNull(E e, Function<E, ?>... etFunctions) {
        //e 等于 null 时，直接返回 true
        if (isNullOrEmpty(e)) {
            return true;
        }

        return ListUtils.adaptArray(etFunctions)
                .collect(fun -> fun.apply(e))
                .anySatisfy(ObjectUtil::isNullOrEmpty);
    }

    /**
     * 任意一个为空或者null
     */
    public static boolean containsNull(Object... objects) {
        //如果本身是空数组，也认为它包含了null
        if (allNull(objects)) {
            return true;
        }

        return ListUtils.adaptArray(objects).anySatisfy(ObjectUtil::isNullOrEmpty);
    }

    /**
     * 含有任意一个不为空的对象
     */
    public static boolean containsNoneNull(Object... objects) {
        return nvl(null, objects) != null;
    }

    public static boolean equals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        } else {
            return object1 != null && object2 != null ? object1.equals(object2) : false;
        }
    }

    /**
     * 将对象转换为Long类型
     * @param data data
     * @return Long
     */
    public static Long convertToLong(Object data) {
        if(data == null){
            return null;
        } else if (data instanceof String) {
            return Long.parseLong((String) data);
        } else if (data instanceof Long) {
            return (Long) data;
        } else if (data instanceof Number) {
            // 兼容 BIGINT UNSIGNED 等驱动返回 BigInteger、聚合/整型列返回 Integer 的场景
            return ((Number) data).longValue();
        } else {
            throw new IllegalArgumentException("Unsupported data type. Expected String or Long.");
        }
    }



    /**
     * 判读对象是否相同
     * @param value1 Object1
     * @param value2 Object2
     * @return Boolean
     */
    public static boolean areObjEqual(Object value1, Object value2) {
        if (value1 == null || value2 == null) {
            return value1 == value2;
        }
        if (value1 instanceof Number && value2 instanceof Number) {
            return ((Number) value1).doubleValue() == ((Number) value2).doubleValue();
        }
        if (value1 instanceof Date && value2 instanceof Date) {
            return ((Date) value1).getTime() == ((Date) value2).getTime();
        }
        return value1.equals(value2);
    }


    /**
     * 转换为BigDecimal，如果为null，则返回0
     * @param o
     * @return
     */
    public static BigDecimal toBigDecimal(Object o){
        return Convert.toBigDecimal(o,BigDecimal.ZERO);
    }

    /**
     * 转换为BigDecimal，如果为null，则返回0
     * @param o
     * @return
     */
    public static BigDecimal toBigDecimal(Object o,Integer scale){
        return Convert.toBigDecimal(o,BigDecimal.ZERO).setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 安全转换为Long（null返回null，支持Number/String类型）
     */
    public static Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.valueOf(val.toString());
    }

    /**
     * 安全转换为Integer（null返回null，支持Number/String类型）
     */
    public static Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.valueOf(val.toString());
    }

    /**
     * 安全转换为LocalDate（null返回null，支持LocalDate/java.sql.Date/String类型）
     */
    public static java.time.LocalDate toLocalDate(Object val) {
        if (val == null) return null;
        if (val instanceof java.time.LocalDate) return (java.time.LocalDate) val;
        if (val instanceof java.sql.Date) return ((java.sql.Date) val).toLocalDate();
        return java.time.LocalDate.parse(val.toString().substring(0, 10));
    }

    /**
     * 安全转换为BigDecimal（null返回null，与原toBigDecimal(o)返回0不同）
     */
    public static java.math.BigDecimal toBigDecimalOrNull(Object val) {
        if (val == null) return null;
        if (val instanceof java.math.BigDecimal) return (java.math.BigDecimal) val;
        return new java.math.BigDecimal(val.toString());
    }
}
