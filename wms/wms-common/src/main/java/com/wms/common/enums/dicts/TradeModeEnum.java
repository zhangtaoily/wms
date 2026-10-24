package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  * 贸易方式数据字典
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum TradeModeEnum {
    GENERAL_TRADE("01", "一般贸易"),
    PROCESSING_WITH_IMPORTED_MATERIALS("02", "进料加工"),
    CUSTOMER_SPECIFIED_MATERIAL("03", "客户指定料"),
    CONTRACTED_MATERIAL("04", "受托来料");

    private final String code;
    private final String name;
}
