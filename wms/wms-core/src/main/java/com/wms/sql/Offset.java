package com.wms.sql;


import com.wms.common.constant.SqlConstant;
import lombok.Data;

@Data
public class Offset implements SqlElement {
    private Integer offset = null;

    @Override
    public String genSqlExpr() {
        if (offset == null) {
            return null;
        }
        return String.format(SqlConstant.OFFSET + " %d", offset);
    }

    @Override
    public String genCountSqlExpr() {
        return null;
    }
}
