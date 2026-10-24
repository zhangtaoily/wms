package com.wms.sql;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.common.exception.ServiceException;
import com.wms.common.rule.ConditionNode;
import com.wms.domain.SysFieldConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 条件树 DSL 编译目标二（MyBatis-Plus QueryWrapper 类型安全 API）单测：
 * getSqlSegment 结构 + paramNameValuePairs 参数绑定断言
 */
public class ConditionTreeApplyToTest {

    private Map<String, SysFieldConfig> whitelist;

    @BeforeEach
    public void setUp() {
        whitelist = new HashMap<>();
        whitelist.put("box_code", field("box_code", "b.box_code"));
        whitelist.put("qty", field("qty", null));
        whitelist.put("status", field("status", "s.status"));
    }

    private static SysFieldConfig field(String columnName, String sqlColumn) {
        SysFieldConfig config = new SysFieldConfig();
        config.setColumnName(columnName);
        config.setSqlColumn(sqlColumn);
        return config;
    }

    private static ConditionNode leaf(String field, String op, Object value) {
        ConditionNode node = new ConditionNode();
        node.setField(field);
        node.setOp(op);
        node.setValue(value);
        return node;
    }

    private static ConditionNode group(String logic, ConditionNode... children) {
        ConditionNode node = new ConditionNode();
        node.setLogic(logic);
        node.setConditions(Arrays.asList(children));
        return node;
    }

    @Test
    public void testApplyLeafEquality() {
        QueryWrapper<Object> wrapper = new QueryWrapper<>();
        ConditionTreeCompiler.applyTo(leaf("box_code", "eq", "A1"), whitelist, wrapper);
        assertTrue(wrapper.getSqlSegment().contains("b.box_code"));
        assertEquals(1, wrapper.getParamNameValuePairs().size());
        assertEquals("A1", wrapper.getParamNameValuePairs().values().iterator().next());
    }

    @Test
    public void testApplyNestedOrGroup() {
        ConditionNode root = group("and",
            leaf("status", "eq", "1"),
            group("or", leaf("qty", "gt", 5), leaf("box_code", "like", "2610%")));
        QueryWrapper<Object> wrapper = new QueryWrapper<>();
        ConditionTreeCompiler.applyTo(root, whitelist, wrapper);
        String segment = wrapper.getSqlSegment();
        assertTrue(segment.contains("s.status"));
        assertTrue(segment.contains("qty"));
        assertTrue(segment.contains("b.box_code"));
        assertTrue(segment.contains(" AND "));
        assertTrue(segment.contains(" OR "));
        // 值经参数绑定，绝不内联进 SQL 文本
        assertFalse(segment.contains("2610"));
        Map<String, Object> params = wrapper.getParamNameValuePairs();
        assertEquals(3, params.size());
        assertTrue(params.containsValue("1"));
        assertTrue(params.containsValue(5));
        assertTrue(params.containsValue("2610%"));
    }

    @Test
    public void testApplyInBetweenIsNullNe() {
        // MP 参数绑定懒求值：须先触发 getSqlSegment() 再断言 paramNameValuePairs
        QueryWrapper<Object> inWrapper = new QueryWrapper<>();
        ConditionTreeCompiler.applyTo(leaf("box_code", "in", Arrays.asList((Object) "A", "B")), whitelist, inWrapper);
        assertTrue(inWrapper.getSqlSegment().contains(" IN "));
        assertTrue(inWrapper.getParamNameValuePairs().containsValue("A"));
        assertTrue(inWrapper.getParamNameValuePairs().containsValue("B"));

        QueryWrapper<Object> betweenWrapper = new QueryWrapper<>();
        ConditionTreeCompiler.applyTo(leaf("qty", "between", "1,9"), whitelist, betweenWrapper);
        String betweenSegment = betweenWrapper.getSqlSegment();
        assertTrue(betweenSegment.contains(" BETWEEN "));
        assertTrue(betweenSegment.contains(" AND "));
        assertEquals(2, betweenWrapper.getParamNameValuePairs().size());
        assertTrue(betweenWrapper.getParamNameValuePairs().containsValue("1"));
        assertTrue(betweenWrapper.getParamNameValuePairs().containsValue("9"));

        QueryWrapper<Object> nullWrapper = new QueryWrapper<>();
        ConditionTreeCompiler.applyTo(leaf("box_code", "isNull", null), whitelist, nullWrapper);
        assertTrue(nullWrapper.getSqlSegment().contains("IS NULL"));
        assertTrue(nullWrapper.getParamNameValuePairs().isEmpty());

        QueryWrapper<Object> neWrapper = new QueryWrapper<>();
        ConditionTreeCompiler.applyTo(leaf("qty", "ne", 0), whitelist, neWrapper);
        assertTrue(neWrapper.getSqlSegment().contains("<>"));
        assertTrue(neWrapper.getParamNameValuePairs().containsValue(0));
    }

    @Test
    public void testApplyGuards() {
        QueryWrapper<Object> wrapper = new QueryWrapper<>();
        // 防线①：白名单外字段拒绝
        assertThrows(ServiceException.class,
            () -> ConditionTreeCompiler.applyTo(leaf("__attack__", "eq", "x"), whitelist, wrapper));
        // 防线②：非法操作符拒绝
        assertThrows(ServiceException.class,
            () -> ConditionTreeCompiler.applyTo(leaf("box_code", "union", "x"), whitelist, wrapper));
        assertTrue(wrapper.getParamNameValuePairs().isEmpty(), "拒绝后不得残留任何绑定参数");
    }

    @Test
    public void testApplyNullSafe() {
        ConditionTreeCompiler.applyTo(null, whitelist, new QueryWrapper<>());
        ConditionTreeCompiler.applyTo(leaf("box_code", "eq", "A1"), whitelist, null);
    }
}
