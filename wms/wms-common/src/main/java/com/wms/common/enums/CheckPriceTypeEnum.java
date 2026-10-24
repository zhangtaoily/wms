package com.wms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author shuang
 */

@Getter
@AllArgsConstructor
public enum CheckPriceTypeEnum {
    KHJUPDATE("KHJUPDATE", "批量修改考核价"),
    XSDD("XSDD", "销售订单"),
    FHTZ("FHTZ", "发货通知单"),
    XSCK("XSCK", "销售出库单");

    private final String code;
    private final String name;
}
