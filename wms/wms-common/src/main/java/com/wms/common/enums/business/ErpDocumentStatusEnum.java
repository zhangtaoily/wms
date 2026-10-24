package com.wms.common.enums.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * erp 单据状态枚举
 * @author shuang
 */
@Getter
@AllArgsConstructor
public enum ErpDocumentStatusEnum {
    A("A", "创建"),
    B("B", "提交中"),
    C("C", "已审核"),
    D("D", "重新审核"),
    Z("Z", "暂存"),
    UNKNOWN("UNKNOWN", "未知状态");
    private final String code;
    private final String name;
    public static ErpDocumentStatusEnum getByCode(String code) {
        for (ErpDocumentStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
