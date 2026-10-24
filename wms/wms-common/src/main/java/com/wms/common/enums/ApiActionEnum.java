package com.wms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApiActionEnum {

    SAVE("新增修改"),
    AUDIT("审核"),
    ANTI_AUDIT("反审核"),
    CANCEL("取消"),
    RE_CANCEL("反取消"),
    UPDATE_MODIFIED("审核后修改");

    private String desc;
}
