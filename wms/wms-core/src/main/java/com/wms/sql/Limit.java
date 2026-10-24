package com.wms.sql;

import com.wms.common.constant.SqlConstant;
import lombok.Data;

@Data
public class Limit implements SqlElement {

    private Integer limit = null;

    @Override
    public String genSqlExpr() {
        if (limit == null) {
            return null;
        }
        return String.format(SqlConstant.LIMIT + " %d", limit);
    }

    @Override
    public String genCountSqlExpr() {
        return null;
    }
}

