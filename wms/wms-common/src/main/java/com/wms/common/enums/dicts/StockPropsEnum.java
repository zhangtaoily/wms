package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  库存属性数据字典
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum StockPropsEnum {
    WAREHOUSE("01", "仓库"),
    STOCK_LOC("02", "仓位"),
    BOM_VERSION("03", "BOM版本"),
    BATCH_NO("04", "批号"),
    PLAN_TRACKING_NO("05", "计划跟踪号");

    private final String code;
    private final String name;
}
