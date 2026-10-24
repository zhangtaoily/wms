package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  * 染色方式数据字典
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum DyeMethodEnum {
    STRIP_DYEING("1", "条染"),
    TWIST_DYEING("2", "绞染"),
    EMBRYO_YARN("3", "胚纱"),
    TWISTING("4", "合股"),
    CONE_DYEING("5", "筒染"),
    OTHER("9", "其他");

    private final String code;
    private final String name;


    public static DyeMethodEnum getByName(String name) {
        for (DyeMethodEnum en : values()) {
            if (en.getName().equals(name)) {
                return en;
            }
        }
        return null;
    }
}
