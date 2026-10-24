package com.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrgEnum {
    XIN_AO(2, "01", "浙江新澳纺织股份有限公司"),
    HONG_KONG(1, "02", "新澳股份（香港）有限公司"),
    VIET_NAM(8, "03", "新澳纺织（越南）有限公司");

    private Integer id;
    private String code;
    private String name;

}
