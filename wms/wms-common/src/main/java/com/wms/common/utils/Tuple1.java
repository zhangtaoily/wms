/**
 * created by leihong.pan at 2018/10/12 上午2:56
 */
package com.wms.common.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Tuple1<T> {
    protected T frist;

    public Tuple1(T t) {
        this.frist = t;
    }
}
