package com.wms.common.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Objects;
import java.util.function.Predicate;

import static com.wms.common.utils.Predicates.Helper.*;

/**
 * 常用断言集合
 */
@SuppressWarnings("all")
@UtilityClass
public class Predicates {

    public boolean equals(Object x, Object y) {
        return Objects.equals(x, y);
    }

    public boolean isNotEquals(Object x, Object y) {
        return !equals(x, y);
    }

    public boolean isNull(Object x) {
        return NULL.test(x);
    }

    public boolean allNull(Object... x) {
        return ALL_NULL.test(x);
    }

    public boolean anyNull(Object... x) {
        return ANY_NULL.test(x);
    }

    public boolean isNotNull(Object x) {
        return NOT_NULL.test(x);
    }

    public boolean allNotNull(Object... x) {
        return ALL_NOT_NULL.test(x);
    }

    public boolean anyNotNull(Object... x) {
        return ANY_NOT_NULL.test(x);
    }

    public boolean isTrue(Boolean bool) {
        return TRUE.test(bool);
    }

    public boolean isFalse(Boolean bool) {
        return FALSE.test(bool);
    }

    public boolean isNullOrFalse(Boolean bool) {
        return NULL.or(FALSE).test(bool);
    }

    /**
     * 全为真
     */
    public boolean allTrue(Boolean... bools) {
        return ListUtils.adaptArray(bools).allSatisfy(TRUE::test);
    }

    /**
     * 任意为真
     */
    public boolean anyTrue(Boolean... bools) {
        return ListUtils.adaptArray(bools).anySatisfy(TRUE::test);
    }

    /**
     * 全为假
     */
    public boolean allFalse(Boolean... bools) {
        return ListUtils.adaptArray(bools).allSatisfy(FALSE::test);
    }

    /**
     * 任意为假
     */
    public boolean anyFalse(Boolean... bools) {
        return ListUtils.adaptArray(bools).anySatisfy(FALSE::test);
    }

    /**
     * 把各个原子语义放到子类中，增加使用成本；应该直接使用方法，而非属性再调方法
     */
    public static class Helper {
        /**
         * null 断言。null 或 空（字符串空串、空集合、空数组）返回 true；其它返回 false。
         */
        public static final Predicate<Object> NULL = ObjectUtil::isNullOrEmpty;
        public static final Predicate<Object[]> ALL_NULL = ObjectUtil::allNull;
        public static final Predicate<Object[]> ANY_NULL = ObjectUtil::containsNull;

        /**
         * 非null 断言。不为 null 或 空（字符串空串、空集合、空数组）返回 true；其它返回 false。
         */
        public static final Predicate<Object> NOT_NULL = ObjectUtil::nonNullOrEmpty;
        public static final Predicate<Object[]> ALL_NOT_NULL = ObjectUtil::allNoneNull;
        public static final Predicate<Object[]> ANY_NOT_NULL = ObjectUtil::containsNoneNull;

        /**
         * true 断言。值为 true 时返回 true；其它返回 false（包括null）
         */
        public static final Predicate<Object> TRUE = x -> x instanceof Boolean && BooleanUtils.isTrue((Boolean) x);

        /**
         * false 断言。值为 false 时返回 true；其它返回 false（包括null）。
         */
        public static final Predicate<Object> FALSE = x -> x instanceof Boolean && BooleanUtils.isFalse((Boolean) x);

        /**
         * 为空或为FALSE
         */
        public static final Predicate<Object> NULL_OR_FLASE = x -> NULL.or(FALSE).test(x);
    }
}