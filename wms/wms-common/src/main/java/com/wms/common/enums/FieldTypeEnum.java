package com.wms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FieldTypeEnum {

    TEXT("文本"),
    NUMBER("数字"),
    NUMBER_RANGE("数字范围"),
    DATE("单个日期"),
    DATETIME("日期时间"),
    DATE_RANGE("日期范围"),
    DATETIME_RANGE("日期时分秒范围"),
    SELECT("下拉选择"),
    BOOL("单选"),
    CHECK_BOX("多选");

    private String desc;
}
