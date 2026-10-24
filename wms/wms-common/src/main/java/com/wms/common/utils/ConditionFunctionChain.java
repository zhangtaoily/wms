package com.wms.common.utils;

import com.wms.common.function.Executable;

import java.util.function.Consumer;
import java.util.function.Function;

public final class ConditionFunctionChain {

    private static class SingletonHelper {
        private static final ConditionFunctionChain INSTANCE = new ConditionFunctionChain();
    }

    public static ConditionFunctionChain getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private ConditionFunctionChain() {
    }

    public <T> ConditionFunctionChain consumerIfNonNull(T t, Consumer<T> consumer) {
        ConditionFunctions.consumerIfNonNull(t, consumer);
        return this;
    }

    public <T, V> ConditionFunctionChain consumerIfNonNull(T t, Function<T, V> tvFunction, Consumer<V> consumer) {
        ConditionFunctions.consumerIfNonNull(t, tvFunction, consumer);
        return this;
    }

    public ConditionFunctionChain executeIfNoneNull(Object x, Executable executable) {
        ConditionFunctions.executeIfNoneNull(x, executable);
        return this;
    }

    public ConditionFunctionChain executeIfTrue(boolean isTrue, Executable executable) {
        ConditionFunctions.executeIfTrue(isTrue, executable);
        return this;
    }
}
