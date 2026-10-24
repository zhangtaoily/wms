package com.wms.common.function;


import java.util.function.Consumer;

/**
 * 空Consumer，啥都不干
 */
public class EmptyConsumer<T> implements Consumer<T> {
    @Override
    public void accept(T t) {
        // 语义就是不进行任何操作
    }
}
