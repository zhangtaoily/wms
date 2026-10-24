package com.wms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author bo
 */
@Getter
@AllArgsConstructor
public enum ProgressEventEnum {
    STARTING(0, "开始运行"), RUNNING(1, "运行中"), COMPLETED(2, "完成导入"), FAILED(3, "失败"), CANCELLED(4, "取消");
    private final int code;
    private final String info;
}
