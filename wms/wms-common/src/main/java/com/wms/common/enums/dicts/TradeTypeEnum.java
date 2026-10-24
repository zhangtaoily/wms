package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  贸易类型数据字典
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TradeTypeEnum {
    INTERNAL_TRADE("01", "内贸"),
    FOREIGN_TRADE("02", "外贸");

    private final String code;
    private final String name;
}
