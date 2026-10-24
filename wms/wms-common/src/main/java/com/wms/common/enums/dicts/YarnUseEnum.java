package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  纱线类型数据字典
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum YarnUseEnum {
    T01("01", "横机"),
    T02("02", "圆机"),
    T03("03", "梭织"),
    T04("04", "梭织围巾"),
    T05("05", "袜子"),
    T06("06", "手编"),
    T07("07", "经编"),
    T08("08", "卖备纱"),
    T09("09", "并线"),
    T10("10", "圆机（无缝）");

    private final String code;
    private final String name;


    public static YarnUseEnum getByName(String name) {
        for (YarnUseEnum en : values()) {
            if (en.getName().equals(name)) {
                return en;
            }
        }
        return null;
    }
}
