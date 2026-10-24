package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计重方式
 */
@Getter
@AllArgsConstructor
public enum WeightTypeEnum {
    STANDARD_WEIGHT_DELIVERY("02", "标重交货"),
    DELIVERY_FULL_POUND("03", "实磅交货"),
    NUMBER_TWISTS("04", "绞数");

    private final String code;
    private final String name;
}
