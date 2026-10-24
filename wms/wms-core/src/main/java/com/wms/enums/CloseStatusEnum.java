package com.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CloseStatusEnum {
    NORMAL(0, "正常"),
    AUTO_CLOSE(1, "自动关闭"),
    MANUAL_CLOSE(2, "手动关闭");

    private final Integer code;
    private final String desc;
}
