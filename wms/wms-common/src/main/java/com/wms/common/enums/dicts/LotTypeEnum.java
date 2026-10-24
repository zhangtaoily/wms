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
public enum LotTypeEnum {
    T001("01", "国产紧密纺"),
    T002("02", "进口紧密纺"),
    T003("03", "赛紧纺"),
    T004("04", "普通纺");

    private final String code;
    private final String name;

    public static LotTypeEnum getByName(String dyeMethodName) {
        for (LotTypeEnum dyeMethodEnum : LotTypeEnum.values()) {
            if (dyeMethodEnum.getName().equals(dyeMethodName)) {
                return dyeMethodEnum;
            }
        }
        return null;
    }
}
