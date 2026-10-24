package com.wms.common.enums.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author li shuang
 */
@Getter
@AllArgsConstructor
public enum ChangeFieldCateEnum {

    HEADER_TABLE(0, "表头"),
    SUB_TABLE(1, "子表");

    private final Integer code;
    private final String name;
}
