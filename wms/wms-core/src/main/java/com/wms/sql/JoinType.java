package com.wms.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 关联类型
 */
@Getter
@AllArgsConstructor
public enum JoinType {
    LEFT("LEFT JOIN"),
    INNER("INNER JOIN");

    private final String expr;
}
