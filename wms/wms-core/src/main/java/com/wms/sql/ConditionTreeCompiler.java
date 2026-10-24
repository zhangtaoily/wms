package com.wms.sql;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.common.exception.ServiceException;
import com.wms.common.rule.CompiledCondition;
import com.wms.common.rule.ConditionNode;
import com.wms.common.rule.ConditionOp;
import com.wms.common.utils.StringUtils;
import com.wms.domain.SysFieldConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 条件树 DSL 编译器（架构规划 3.4）：一棵树，两个编译目标。
 * <ul>
 *   <li>{@link #compile} → (fragment, params)，交 CustomizeMapper 参数化重载执行（多表链路 DataViewMetaData）；</li>
 *   <li>{@link #applyTo} → MyBatis-Plus QueryWrapper 类型安全 API（单表链路）。</li>
 * </ul>
 * 三层防线：① field 必须命中白名单（sys_field_config 注册，列表达式取元数据 sqlColumn）；
 * ② op 仅允许 ConditionOp 枚举；③ 值只经 ? / #{ctP.pN} 绑定，永不内联进 SQL 文本。
 *
 * @author wms
 */
public class ConditionTreeCompiler {

    private ConditionTreeCompiler() {
    }

    /**
     * 编译目标一：条件树 → SQL 片段 + 参数列表。
     * 根组不包裹括号（由调用方拼接），嵌套组包裹括号。
     */
    public static CompiledCondition compile(ConditionNode root, Map<String, SysFieldConfig> whitelist) {
        if (root == null) {
            return new CompiledCondition("", new ArrayList<>());
        }
        return compileNode(root, whitelist);
    }

    /**
     * 编译目标二：条件树 → MyBatis-Plus QueryWrapper（类型安全 API，值经参数绑定）
     */
    public static <T> void applyTo(ConditionNode root, Map<String, SysFieldConfig> whitelist, QueryWrapper<T> wrapper) {
        if (root == null || wrapper == null) {
            return;
        }
        applyNode(root, whitelist, wrapper);
    }

    private static CompiledCondition compileNode(ConditionNode node, Map<String, SysFieldConfig> whitelist) {
        if (node.isGroup()) {
            String logic = resolveLogic(node.getLogic());
            StringBuilder fragment = new StringBuilder();
            List<Object> params = new ArrayList<>();
            boolean first = true;
            for (ConditionNode child : node.getConditions()) {
                if (child == null) {
                    continue;
                }
                CompiledCondition childCompiled = compileNode(child, whitelist);
                if (!first) {
                    fragment.append(logic);
                }
                if (child.isGroup()) {
                    fragment.append('(').append(childCompiled.getFragment()).append(')');
                } else {
                    fragment.append(childCompiled.getFragment());
                }
                params.addAll(childCompiled.getParams());
                first = false;
            }
            return new CompiledCondition(fragment.toString(), params);
        }
        SysFieldConfig meta = whitelist.get(node.getField());
        if (meta == null) {
            // 防线①：列名白名单，未注册字段直接拒绝
            throw new ServiceException("字段未注册或不可查询: " + node.getField());
        }
        // 防线②：操作符枚举严格校验
        ConditionOp op = ConditionOp.of(node.getOp());
        return op.compileLeaf(resolveColumn(meta), node.getValue());
    }

    private static <T> void applyNode(ConditionNode node, Map<String, SysFieldConfig> whitelist, QueryWrapper<T> wrapper) {
        if (node.isGroup()) {
            String logic = resolveLogic(node.getLogic());
            boolean first = true;
            for (ConditionNode child : node.getConditions()) {
                if (child == null) {
                    continue;
                }
                if (first) {
                    applyNode(child, whitelist, wrapper);
                    first = false;
                } else if (" OR ".equals(logic)) {
                    wrapper.or(nw -> applyNode(child, whitelist, nw));
                } else {
                    wrapper.and(nw -> applyNode(child, whitelist, nw));
                }
            }
        } else {
            applyLeaf(node, whitelist, wrapper);
        }
    }

    private static void applyLeaf(ConditionNode node, Map<String, SysFieldConfig> whitelist, QueryWrapper<?> wrapper) {
        SysFieldConfig meta = whitelist.get(node.getField());
        if (meta == null) {
            throw new ServiceException("字段未注册或不可查询: " + node.getField());
        }
        ConditionOp op = ConditionOp.of(node.getOp());
        String column = resolveColumn(meta);
        Object value = node.getValue();
        switch (op) {
            case EQ:
                wrapper.eq(column, value);
                break;
            case NE:
                wrapper.ne(column, value);
                break;
            case GT:
                wrapper.gt(column, value);
                break;
            case GE:
                wrapper.ge(column, value);
                break;
            case LT:
                wrapper.lt(column, value);
                break;
            case LE:
                wrapper.le(column, value);
                break;
            case LIKE:
                // 值原样绑定（前端控制通配符），与 SQL 编译目标语义一致
                wrapper.apply(column + " LIKE {0}", value);
                break;
            case IN:
                wrapper.in(column, ConditionOp.inValues(value));
                break;
            case BETWEEN:
                List<Object> range = ConditionOp.betweenValues(value);
                wrapper.between(column, range.get(0), range.get(1));
                break;
            case IS_NULL:
                wrapper.isNull(column);
                break;
            default:
                throw new ServiceException("非法条件操作符: " + node.getOp());
        }
    }

    private static String resolveColumn(SysFieldConfig meta) {
        return StringUtils.isNotBlank(meta.getSqlColumn()) ? meta.getSqlColumn() : meta.getColumnName();
    }

    private static String resolveLogic(String logic) {
        if (StringUtils.isBlank(logic) || "and".equalsIgnoreCase(logic.trim())) {
            return " AND ";
        }
        if ("or".equalsIgnoreCase(logic.trim())) {
            return " OR ";
        }
        throw new ServiceException("非法条件逻辑符: " + logic);
    }
}
