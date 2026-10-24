package com.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author li_shuang
 */

@Getter
@AllArgsConstructor
public enum CancelStatusEnum {
    NORMAL(0, "正常"),
    CANCEL(1, "取消");

    private final Integer code;
    private final String desc;
}
