package com.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author li_shuang
 */

@Getter
@AllArgsConstructor
public enum CancelOaStatusEnum {
    NORMAL(0, "正常"),
    CANCEL_OA_ING(1, "取消审批中"),
    CANCEL_OA_PASS(2, "取消审批通过"),
    CANCEL_OA_REJECT(3, "取消审批驳回");

    private final Integer code;
    private final String desc;
}
