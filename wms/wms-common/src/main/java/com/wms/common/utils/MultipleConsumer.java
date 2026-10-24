package com.wms.common.utils;

/**
 * @author zhangtao
 */
@FunctionalInterface
public interface MultipleConsumer<T, R> {
    /**
     * 接受两个对象并处理，source对象为调用copy传入的对象，target为copy生成的对象
     *
     * @param source 原始对象
     * @param target 目标对象
     */
    void accept(T source, R target);
}
