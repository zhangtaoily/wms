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
public enum YarnTypeEnum {
    ORDINARY_YARN("01", "普纱"),
    RD("02", "研发"),
    NEW_TYPE_YARN("03", "新型纱");

    private final String code;
    private final String name;


    public static YarnTypeEnum getByName(String name) {
        for (YarnTypeEnum en : values()) {
            if (en.getName().equals(name)) {
                return en;
            }
        }
        return null;
    }
}
