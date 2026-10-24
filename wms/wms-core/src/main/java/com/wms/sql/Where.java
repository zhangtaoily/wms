package com.wms.sql;

import com.wms.common.constant.SqlConstant;
import lombok.Data;

@Data
public class Where implements SqlElement {
    private String expr;

    public static Where of(String expr) {
        Where x = new Where();
        x.setExpr(expr);
        return x;
    }

    @Override
    public String genSqlExpr() {
        return SqlConstant.WHERE + expr;
    }

    @Override
    public String genCountSqlExpr() {
        return null;
    }
}
