package com.wms.common.rule;

import com.wms.common.exception.ServiceException;
import com.wms.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 条件树操作符白名单（架构规划 3.4：eq/ne/gt/ge/lt/le/like/in/between/isNull）。
 * <p>叶子编译只产出 {@code ?} 占位片段 + 顺序参数列表，值永不内联进 SQL 文本（三层防线之三）。</p>
 *
 * @author wms
 */
public enum ConditionOp {

    EQ("="),
    NE("!="),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    LIKE("LIKE"),
    IN("IN"),
    BETWEEN("BETWEEN"),
    IS_NULL("IS NULL");

    private final String symbol;

    ConditionOp(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * 严格解析操作符（三层防线之二）：未知或空白 op 直接拒绝
     */
    public static ConditionOp of(String op) {
        if (StringUtils.isNotBlank(op)) {
            // 下划线归一化：DSL 白名单原文 isNull 与枚举名 IS_NULL 等价
            String normalized = op.trim().replace("_", "");
            for (ConditionOp candidate : values()) {
                if (candidate.name().replace("_", "").equalsIgnoreCase(normalized)) {
                    return candidate;
                }
            }
        }
        throw new ServiceException("非法条件操作符: " + op);
    }

    /**
     * 编译叶子为 {@code ?} 占位片段 + 参数列表
     *
     * @param column 列表达式（来自白名单元数据 sqlColumn，非用户输入）
     * @param value  用户值（只进参数列表）
     */
    public CompiledCondition compileLeaf(String column, Object value) {
        List<Object> params = new ArrayList<>();
        StringBuilder fragment = new StringBuilder("(").append(column).append(' ');
        switch (this) {
            case IS_NULL:
                fragment.append("IS NULL)");
                break;
            case IN:
                List<Object> inItems = inValues(value);
                fragment.append("IN (");
                for (int i = 0; i < inItems.size(); i++) {
                    if (i > 0) {
                        fragment.append(", ");
                    }
                    fragment.append('?');
                    params.add(inItems.get(i));
                }
                fragment.append("))");
                break;
            case BETWEEN:
                List<Object> range = betweenValues(value);
                fragment.append("BETWEEN ? AND ?)");
                params.add(range.get(0));
                params.add(range.get(1));
                break;
            default:
                requireValue(value, name());
                fragment.append(symbol).append(" ?)");
                params.add(value);
                break;
        }
        return new CompiledCondition(fragment.toString(), params);
    }

    /**
     * IN 值归一化：集合原样收集，字符串按 [,;] 切分；空值拒绝
     */
    public static List<Object> inValues(Object value) {
        List<Object> result = new ArrayList<>();
        if (value instanceof Collection) {
            for (Object item : (Collection<?>) value) {
                if (item != null && StringUtils.isNotBlank(String.valueOf(item))) {
                    result.add(item);
                }
            }
        } else if (value != null) {
            for (String item : String.valueOf(value).split("[,;]")) {
                if (StringUtils.isNotBlank(item)) {
                    result.add(item.trim());
                }
            }
        }
        if (result.isEmpty()) {
            throw new ServiceException("IN 条件值不能为空");
        }
        return result;
    }

    /**
     * BETWEEN 值归一化：双元素集合，或字符串按 [;,] 切分为两段
     */
    public static List<Object> betweenValues(Object value) {
        List<Object> values = new ArrayList<>();
        if (value instanceof Collection) {
            values.addAll((Collection<?>) value);
        } else if (value != null) {
            values.addAll(Arrays.asList(String.valueOf(value).split("[;,]")));
        }
        if (values.size() != 2) {
            throw new ServiceException("BETWEEN 条件需要且仅需要两个值");
        }
        return values;
    }

    private static void requireValue(Object value, String opName) {
        if (value == null
            || (value instanceof Collection && ((Collection<?>) value).isEmpty())
            || (!(value instanceof Collection) && StringUtils.isBlank(String.valueOf(value)))) {
            throw new ServiceException(opName + " 条件值不能为空");
        }
    }
}
