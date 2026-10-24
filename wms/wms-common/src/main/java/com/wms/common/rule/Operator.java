package com.wms.common.rule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.wms.common.enums.FieldTypeEnum;
import com.wms.common.utils.ListUtils;
import com.wms.common.utils.StringUtils;

/**
 * 三参数函数接口，用于构建SQL语句
 */
@FunctionalInterface
interface StatementBuilder {
    String build(String column, String value, FieldTypeEnum fieldType);
}

public enum Operator {
    AND("AND", 1, (column, value, fieldType) -> null),
    OR("OR", 1, (column, value, fieldType) -> null), //逻辑运算符

    EQ("=", 2, (column, value, fieldType) -> {
        if (fieldType == FieldTypeEnum.NUMBER) {
            return String.format("(%s = %s)", column, value);
        }
        if (fieldType == FieldTypeEnum.TEXT) {
            return String.format("(%s = '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATETIME_RANGE) {
            return String.format("(%s = '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATE || fieldType == FieldTypeEnum.DATE_RANGE) {
            return String.format("(date(%s) = date('%s'))", column, value);
        }
        return null;
    }),

    NE("!=", 2, (column, value, fieldType) -> {
        if (fieldType == FieldTypeEnum.NUMBER) {
            return String.format("(%s != %s)", column, value);
        }
        if (fieldType == FieldTypeEnum.TEXT) {
            return String.format("(%s != '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATETIME_RANGE) {
            return String.format("(%s != '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATE || fieldType == FieldTypeEnum.DATE_RANGE) {
            return String.format("(date(%s) != date('%s'))", column, value);
        }
        return null;
    }),

    GT(">", 2, (column, value, fieldType) -> {
        if (fieldType == FieldTypeEnum.NUMBER) {
            return String.format("(%s > %s)", column, value);
        }
        if (fieldType == FieldTypeEnum.TEXT) {
            return String.format("(%s > '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATETIME_RANGE) {
            return String.format("(%s > '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATE || fieldType == FieldTypeEnum.DATE_RANGE) {
            return String.format("(date(%s) > date('%s'))", column, value);
        }
        return null;
    }),

    GE(">=", 2, (column, value, fieldType) -> {
        if (fieldType == FieldTypeEnum.NUMBER) {
            return String.format("(%s >= %s)", column, value);
        }
        if (fieldType == FieldTypeEnum.TEXT) {
            return String.format("(%s >= '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATETIME_RANGE) {
            return String.format("(%s >= '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATE || fieldType == FieldTypeEnum.DATE_RANGE) {
            return String.format("(date(%s) >= date('%s'))", column, value);
        }
        return null;
    }),

    LT("<", 2, (column, value, fieldType) -> {
        if (fieldType == FieldTypeEnum.NUMBER) {
            return String.format("(%s < %s)", column, value);
        }
        if (fieldType == FieldTypeEnum.TEXT) {
            return String.format("(%s < '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATETIME_RANGE) {
            return String.format("(%s < '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATE || fieldType == FieldTypeEnum.DATE_RANGE) {
            return String.format("(date(%s) < date('%s'))", column, value);
        }
        return null;
    }),

    LE("<=", 2, (column, value, fieldType) -> {
        if (fieldType == FieldTypeEnum.NUMBER) {
            return String.format("(%s <= %s)", column, value);
        }
        if (fieldType == FieldTypeEnum.TEXT) {
            return String.format("(%s <= '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATETIME_RANGE) {
            return String.format("(%s <= '%s')", column, value);
        }
        if (fieldType == FieldTypeEnum.DATE || fieldType == FieldTypeEnum.DATE_RANGE) {
            return String.format("(date(%s) <= date('%s'))", column, value);
        }
        return null;
    }), // 单参数比较

    GE_LE(GE, LE, 6, (column, value, fieldType) -> {
        String[] range = value.split(";");
        if (fieldType == FieldTypeEnum.NUMBER_RANGE) {
            return String.format("(%s >=%s and %s <=%s)", column, range[0], column, range[1]);
        }
        if (fieldType == FieldTypeEnum.DATE_RANGE) {
            return String.format("(%s >='%s 00:00:00' and %s <='%s 23:59:59')", column, range[0], column, range[1]);
        }
        if (fieldType == FieldTypeEnum.DATETIME_RANGE) {
            return String.format("(%s >='%s 00:00:00' and %s <='%s 23:59:59')", column, range[0], column, range[1]);
        }
        return null;
    }), // 双参数比较

    IN("IN", 10, (column, value, fieldType) -> {
        if (value != null && !value.trim().isEmpty()) {
            if (fieldType == FieldTypeEnum.NUMBER) {
                Object[] multValue = value.split(",");
                return String.format("(%s IN %s)", column, ListUtils.toNumberIn(multValue));
            }
            if (fieldType == FieldTypeEnum.TEXT) {
                String[] multValue = value.split(",");
                return String.format("(%s IN %s)", column, ListUtils.toTextIn(multValue));
            }
        }
        return null;
    }),

    NOT_IN("NOT IN", 10, (column, value, fieldType) -> {
        if (value != null && !value.trim().isEmpty()) {
            if (fieldType == FieldTypeEnum.NUMBER) {
                Object[] multValue = value.split(",");
                return String.format("(%s NOT IN %s)", column, ListUtils.toNumberIn(multValue));
            }
            if (fieldType == FieldTypeEnum.TEXT) {
                String[] multValue = value.split(",");
                return String.format("(%s NOT IN %s)", column, ListUtils.toTextIn(multValue));
            }
        }
        return null;
    }),

    FIND_IN_SET("FIND_IN_SET", 10, (column, value, fieldType) -> {
        return String.format("(FIND_IN_SET(%s,%s))", value, column);
    }), // 集合比较

    LIKE("LIKE", 18, (column, value, fieldType) -> {
        return String.format("(%s LIKE '%%%s%%')", column, value);
    }),

    NOT_LIKE("NOT LIKE", 18, (column, value, fieldType) -> {
        return String.format("(%s NOT LIKE '%%%s%%')", column, value);
    }), // 模糊匹配

    L_LIKE("LIKE", 18, (column, value, fieldType) -> {
        return String.format("(%s LIKE '%s%%')", column, value);
    }),

    R_LIKE("LIKE", 18, (column, value, fieldType) -> {
        return String.format("(%s LIKE '%%%s')", column, value);
    }),

    L_LIKE_IN_OR("LIKE",18, (column, value, fieldType) -> {
        String[] valuesArray = value.split(",");
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < valuesArray.length; i++) {
            if (i > 0) {
                inClause.append(" OR ");
            }
            inClause.append(String.format("(%s LIKE '%s%%')", column, valuesArray[i]));
        }
        return "(" + inClause + ")";
    }),

    TODAY("TODAY",102, (column, value, fieldType) -> {
        return String.format("(%s >= CURDATE() AND %s < DATE_ADD(CURDATE(), INTERVAL 1 DAY))", column, column);
    }),

    YESTERDAY("YESTERDAY",103, (column, value, fieldType) -> {
        return String.format("(%s >= DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND %s < CURDATE())", column, column);
    }),

    TOMORROW("TOMORROW",104, (column, value, fieldType) -> {
        return String.format("(%s >= DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND %s < DATE_ADD(CURDATE(), INTERVAL 2 DAY))", column, column);
    }),

    THREEDAYSBEFORE("THREEDAYSBEFORE",105, (column, value, fieldType) -> {
        return String.format("(%s >= DATE_SUB(CURDATE(), INTERVAL 3 DAY) AND %s < CURDATE())", column, column);
    }),

    LASTWEEK("LASTWEEK",106, (column, value, fieldType) -> {
        return String.format("(%s >= DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) + 1 DAY), INTERVAL 1 WEEK) " +
            "AND %s < DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) + 1 DAY), INTERVAL 0 WEEK))", column, column);
    }),

    LASTMONTH("LASTMONTH",107, (column, value, fieldType) -> {
        return String.format("(%s >= DATE_SUB(LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 2 MONTH)), INTERVAL DAYOFMONTH(DATE_SUB(CURDATE(), INTERVAL 2 MONTH)) - 1 DAY) " +
            "AND %s < DATE_SUB(LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), INTERVAL DAYOFMONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) - 1 DAY))", column, column);
    }),

    THISMONTH("THISMONTH",108, (column, value, fieldType) -> {
        return String.format("(%s >= DATE_SUB(CURDATE(), INTERVAL DAYOFMONTH(CURDATE()) - 1 DAY) AND %s < DATE_ADD(LAST_DAY(CURDATE()), INTERVAL 1 DAY))", column, column);
    }),

    IS_NULL("IS NULL", 19, (column, value, fieldType) -> {
        return String.format("(%s IS NULL)", column);
    }),

    IS_NOT_NULL("IS NOT NULL", 19, (column, value, fieldType) -> {
        return String.format("(%s IS NOT NULL)", column);
    }), // NULL运算符

    UNKNOWN("UNKNOW", 0, (column, value, fieldType) -> null); // 未知

    private String symbol;
    private Operator first;
    private Operator second;
    private int opType;
    private StatementBuilder statementBuilder;

    Operator(String symbol, int opType, StatementBuilder statementBuilder) {
        this.symbol = symbol;
        this.opType = opType;
        this.statementBuilder = statementBuilder;
    }

    Operator(Operator first, Operator second, int opType, StatementBuilder statementBuilder) {
        this(first.symbol, opType, statementBuilder);
        this.first = first;
        this.second = second;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSecondSymbol() {
        return null != second ? second.symbol : null;
    }

    public Operator getFirst() {
        return first;
    }

    public Operator getSecond() {
        return second;
    }

    /**
     * 生成SQL语句
     *
     * @param column 列名
     * @param value 值
     * @param fieldType 字段类型
     * @return SQL语句
     */
    public String buildStatement(String column, String value, FieldTypeEnum fieldType) {
        return statementBuilder.build(column, value, fieldType);
    }

    @JsonCreator
    public static Operator fromValue(Object v) {
        if (null == v) {
            return UNKNOWN;
        }
        try {
            return Operator.valueOf(v.toString());
        } catch (Throwable t) {
            return UNKNOWN;
        }
    }
}
