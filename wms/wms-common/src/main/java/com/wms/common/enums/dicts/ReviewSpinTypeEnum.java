package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  * 评审纺纱类型
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ReviewSpinTypeEnum {
    T001("001", "普通纺"),
    T002("002", "进口紧密纺"),
    T003("003", "国产紧密纺"),
    T004("004", "赛紧纺");

    private final String code;
    private final String name;

    public static ReviewSpinTypeEnum getByName(String name) {
        for (ReviewSpinTypeEnum dyeMethodEnum : values()) {
            if (dyeMethodEnum.getName().equals(name)) {
                return dyeMethodEnum;
            }
        }
        return null;
    }
}
