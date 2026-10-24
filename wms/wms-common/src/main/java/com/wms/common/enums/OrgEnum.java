package com.wms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrgEnum {
    XIN_AO("01", "浙江新澳纺织股份有限公司", 2),
    HONG_KONG("02", "香港", 1),
    VIETNAM("03", "新澳纺织（越南）有限公司", 8),
    YIN_CHUAN("04", "新澳纺织（银川）有限公司", 7),
    NING_XIN("08", "宁夏新澳羊绒有限公司", 4);

    private final String code;
    private final String name;
    private final Integer id;
}
