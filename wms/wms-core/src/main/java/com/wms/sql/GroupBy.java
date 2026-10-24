package com.wms.sql;

import com.wms.common.constant.SqlConstant;
import com.wms.common.utils.ListUtils;
import lombok.Data;

import java.util.List;

@Data
public class GroupBy implements SqlElement {

    private List<String> fields;

    @Override
    public String genSqlExpr() {
        if (ListUtils.isEmpty(fields)) {
            return null;
        }

        return String.format(SqlConstant.GROUP_BY + " %s", ListUtils.makeString(fields, ","));
    }

    @Override
    public String genCountSqlExpr() {
        return null;
    }
}

