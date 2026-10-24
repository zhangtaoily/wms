package com.wms.common.enums;

import lombok.Getter;

/**
 * ERP单据key枚举
 * @author li_shuang
 */

@Getter
public enum ExpressType {

    KUA_YUE("2.3","跨越速运"),

    YI_MI_DI_DA("20.2","壹米滴答"),

    BEST("2.1","百世快运");

    private final String value;
    private final String description;

    ExpressType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.name();
    }
}

