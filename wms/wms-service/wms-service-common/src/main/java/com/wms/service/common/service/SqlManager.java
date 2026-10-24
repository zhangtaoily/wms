package com.wms.service.common.service;

import com.wms.common.constant.SqlConstant;
import com.wms.common.enums.OrderByEnum;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 构造SQL的类
 */
@Builder
public class SqlManager {

    private String tableName;

    private String mainKeyColumn;

    private List<String> fields;

    private List<OrderBy> orderBy;

    private Integer limit;

    private Integer offset;

    private String where;

    public String generateSql() {
        String sql = SqlConstant.SELECT + fetchField();
        sql += SqlConstant.FROM + tableName;

        if (StringUtils.isNotEmpty(where)) {
            sql += SqlConstant.WHERE + where;
        }

        if (ListUtils.isNotEmpty(orderBy)) {
            sql += SqlConstant.ORDER_BY;
            List<String> orders = ListUtils.empty();
            for (OrderBy order : orderBy) {
                orders.add(order.field + " " + order.model);
            }
            sql += ListUtils.makeString(orders, ",");
        }

        if (limit != null) {
            sql += SqlConstant.LIMIT + limit;
        }

        if (offset != null) {
            sql += SqlConstant.OFFSET + offset;
        }

        return sql;
    }

    private String fetchField() {
        return ListUtils.makeString(fields, ",");
    }

    public String generateCountSql() {
        String sql = SqlConstant.SELECT + SqlConstant.COUNT + SqlConstant.FROM + tableName;
        if (StringUtils.isNotEmpty(where)) {
            sql += SqlConstant.WHERE + where;
        }
        return sql;
    }

    @Data
    @AllArgsConstructor
    public static class OrderBy {
        private String field;
        private Integer sortIndex;
        private OrderByEnum model;
    }
}
