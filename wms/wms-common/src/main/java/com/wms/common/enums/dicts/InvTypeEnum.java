package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 备货类型数据字段
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum InvTypeEnum {
    E1("1", "增值税发票"),
    E2("2", "普通发票"),
    E3("3", "外销发票");

    private final String code;
    private final String name;
}
