package com.wms.common.enums.dicts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *  接口触发条件数据字典
 *
 * @author lishuang
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum WorkTimingEnum {
    AFTER_ACTIVATION(1, "生效后"),
    BEFORE_ACTIVATION(2, "生效前"),
    BEFORE_CANCEL(3, "撤销前"),
    MANUALLY_TRIGGERED(4, "手动触发"),
    MODIFY_CHECK_PRICE(5, "修改考核价"),
    TECH_REVIEW(6, "技术评审"),
    PLAN_REVIEW(7, "计划评审"),
    UPDATE_TRACKING_NUMBER(8, "更新运单号");

    private final int code;
    private final String name;

}
