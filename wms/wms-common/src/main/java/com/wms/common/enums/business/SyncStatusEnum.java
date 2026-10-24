package com.wms.common.enums.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 单据同步状态
 * @author li-shuang
 */

@Getter
@AllArgsConstructor
public enum SyncStatusEnum {
    FAIL("0", "同步失败"),
    SUCCESS("1", "同步成功");

    private final String code;
    private final String name;
}
