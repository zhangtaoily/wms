
package com.wms.common.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Tuple2<T, K> extends Tuple1<T> {

    protected K second;

    public Tuple2(T t, K k) {
        super(t);
        this.second = k;
    }

    public static <T, K> Tuple2<T, K> of(T first, K second) {
        return new Tuple2<>(first, second);
    }

    public Boolean isSuccess() {
        if (this.getFrist() != null) {
            if (this.getFrist().getClass() == Integer.class) {
                return (Integer) this.getFrist() != 0 && (Integer) this.getFrist() != 2;
            }
            if (this.getFrist().getClass() == Boolean.class) {
                return (Boolean) this.getFrist();
            }
            return true;
        } else {
            return false;
        }
    }
}
