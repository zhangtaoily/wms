package com.wms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 功能模块枚举（WMS）
 */
@AllArgsConstructor
@Getter
public enum FunctionEnum {

    PRINT_CONFIG("PRINT_CONFIG", "打印模板类型"),
    FIELD_CONFIG("FIELD_CONFIG", "字段配置"),
    SYS_EMPLOYEE("SYS_EMPLOYEE", "员工管理"),
    SYS_INTERFACE_CFG("SYS_INTERFACE_CFG", "接口配置"),
    SYS_INTERFACE_LOG("SYS_INTERFACE_LOG", "接口日志"),
    SYS_BUSINESSFLOW_CFG("SYS_BUSINESSFLOW_CFG", "接口业务流配置"),
    SYS_AUTO_CODE_SET("SYS_AUTO_CODE_SET", "自动编码"),
    SYS_DICT_DATA("SYS_DICT_DATA", "数据字典"),
    SYS_FILTER_SCHEME("SYS_FILTER_SCHEME", "过滤方案模块"),
    SYS_DATA_IMPORT_RECORD("DATA_IMPORT_RECORD","数据导入列表"),
    B_FIELD_SHOW("B_FIELD_SHOW", "字段显示隐藏模块"),
    WAREHOUSE("WAREHOUSE", "仓库信息"),
    LOCATION("LOCATION", "仓位信息"),
    BIZ_BATCH("BIZ_BATCH", "业务批次"),
    MATERIAL("MATERIAL", "物料管理"),
    INV_BATCH("INV_BATCH", "库存批次"),
    BOX_INFO("BOX_INFO", "箱码信息"),
    PALLET_INFO("PALLET_INFO", "托盘信息"),
    PRINT_TEMPLATE("PRINT_TEMPLATE", "打印模板"),
    REMIND_SETTING("REMIND_SETTING", "提醒设置"),
    WMS_RECEIVE("WMS_RECEIVE", "收货清单"),
    WMS_CODE_COLLECT("WMS_CODE_COLLECT", "码单收录"),
    WMS_PALLET_ORDER("WMS_PALLET_ORDER", "组托单"),
    WMS_INBOUND("WMS_INBOUND", "入库单"),
    WMS_OUT_NOTICE("WMS_OUT_NOTICE", "出库通知"),
    WMS_OUTBOUND("WMS_OUTBOUND", "出库单"),
    WMS_STOCK_MOVE("WMS_STOCK_MOVE", "移库单"),
    WMS_STOCK_COUNT("WMS_STOCK_COUNT", "盘存单"),
    SYNC_RECORD("SYNC_RECORD", "同步监控"),
    ASRS_WH_TASK("ASRS_WH_TASK", "仓储任务"),
    ASRS_OP_TASK("ASRS_OP_TASK", "作业任务"),
    ASRS_OP_LOG("ASRS_OP_LOG", "作业日志");

    private final String code;
    private final String desc;

    public static FunctionEnum fromCode(String code) {
        for (FunctionEnum function : FunctionEnum.values()) {
            if (function.getCode().equals(code)) {
                return function;
            }
        }
        throw new IllegalArgumentException("Unknown function code: " + code);
    }
}
