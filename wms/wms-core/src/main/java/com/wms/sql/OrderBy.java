package com.wms.sql;

import com.wms.common.constant.SqlConstant;
import com.wms.common.enums.OrderByEnum;
import com.wms.common.utils.ListUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class OrderBy implements SqlElement {

    private List<OrderByOption> orderByOption =ListUtils.empty();

    @Override
    public String genSqlExpr() {
        if(ListUtils.isEmpty(orderByOption)){
            return null;
        }
        String orderBySql = SqlConstant.ORDER_BY;
        List<String> orders = ListUtils.empty();
        for (OrderByOption order : orderByOption) {
            orders.add(order.field + " " + order.model);
        }
        orderBySql += ListUtils.makeString(orders, ",");
        return orderBySql;
    }

    @Override
    public String genCountSqlExpr() {
        return null;
    }

    @Data
    @AllArgsConstructor
    public static class OrderByOption {
        private String field;
        private Integer sortIndex;
        private OrderByEnum model;
    }
}
