package com.wms.sql;

import com.wms.common.exception.ServiceException;
import com.wms.common.rule.CompiledCondition;
import com.wms.common.rule.ConditionNode;
import com.wms.domain.SysFieldConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 条件树 DSL 编译目标一（SQL 片段 + 参数列表）单测：
 * 嵌套树编译、10 操作符全覆盖、参数顺序、三层防线、toMybatisFragment 绑定改写
 */
public class ConditionTreeCompilerTest {

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
        node.setConditions(new ArrayList<>(Arrays.asList(children)));
        return node;
    }

    private void assertLeaf(String op, Object value, String expectedFragment, int expectedParams) {
        CompiledCondition compiled = ConditionTreeCompiler.compile(leaf("box_code", op, value), whitelist);
        assertEquals(expectedFragment, compiled.getFragment());
        assertEquals(expectedParams, compiled.getParams().size());
    }

    @Test
    public void testNullTreeReturnsEmptyProduct() {
        CompiledCondition compiled = ConditionTreeCompiler.compile(null, whitelist);
        assertEquals("", compiled.getFragment());
        assertTrue(compiled.getParams().isEmpty());
    }

    @Test
    public void testTenOperatorsCompile() {
        // 值优先命中元数据 sqlColumn
        assertLeaf("eq", "A1", "(b.box_code = ?)", 1);
        assertLeaf("ne", "A1", "(b.box_code != ?)", 1);
        assertLeaf("like", "2610%", "(b.box_code LIKE ?)", 1);
        assertLeaf("in", "A,B;C", "(b.box_code IN (?, ?, ?))", 3);
        assertLeaf("isNull", null, "(b.box_code IS NULL)", 0);
        // 无 sqlColumn 时回退 columnName
        assertEquals("(qty > ?)",
            ConditionTreeCompiler.compile(leaf("qty", "gt", 10), whitelist).getFragment());
        assertEquals("(qty >= ?)",
            ConditionTreeCompiler.compile(leaf("qty", "ge", 10), whitelist).getFragment());
        assertEquals("(qty < ?)",
            ConditionTreeCompiler.compile(leaf("qty", "lt", 10), whitelist).getFragment());
        assertEquals("(qty <= ?)",
            ConditionTreeCompiler.compile(leaf("qty", "le", 10), whitelist).getFragment());
        assertEquals("(qty BETWEEN ? AND ?)",
            ConditionTreeCompiler.compile(leaf("qty", "between", "2026-10-01,2026-10-19"), whitelist).getFragment());
    }

    @Test
    public void testInFromCollection() {
        CompiledCondition compiled = ConditionTreeCompiler.compile(
            leaf("box_code", "in", Arrays.asList((Object) "A", "B")), whitelist);
        assertEquals("(b.box_code IN (?, ?))", compiled.getFragment());
        assertEquals(Arrays.asList("A", "B"), compiled.getParams());
    }

    @Test
    public void testNestedTreeCompilationAndParamOrder() {
        ConditionNode root = group("or",
            leaf("box_code", "like", "2610%"),
            group("and", leaf("qty", "gt", 10), leaf("status", "eq", "1")));
        CompiledCondition compiled = ConditionTreeCompiler.compile(root, whitelist);
        // 子组带括号、根组不带（由调用方拼接）
        assertEquals("(b.box_code LIKE ?) OR ((qty > ?) AND (s.status = ?))", compiled.getFragment());
        // 参数顺序与 ? 出现顺序严格一致
        assertEquals(Arrays.asList("2610%", 10, "1"), compiled.getParams());
    }

    @Test
    public void testGuard1UnregisteredFieldRejected() {
        ServiceException ex = assertThrows(ServiceException.class,
            () -> ConditionTreeCompiler.compile(leaf("__attack__ or 1=1--", "eq", "x"), whitelist));
        assertTrue(ex.getMessage().contains("字段未注册或不可查询"));
    }

    @Test
    public void testGuard2IllegalOpRejected() {
        ServiceException ex = assertThrows(ServiceException.class,
            () -> ConditionTreeCompiler.compile(leaf("box_code", "regex", "x"), whitelist));
        assertTrue(ex.getMessage().contains("非法条件操作符"));
        // 空白 op 同样拒绝
        assertThrows(ServiceException.class,
            () -> ConditionTreeCompiler.compile(leaf("box_code", "  ", "x"), whitelist));
    }

    @Test
    public void testGuard3ValueNeverInlined() {
        String injection = "' or '1'='1";
        CompiledCondition compiled = ConditionTreeCompiler.compile(leaf("box_code", "eq", injection), whitelist);
        assertEquals("(b.box_code = ?)", compiled.getFragment());
        assertFalse(compiled.getFragment().contains("'"), "SQL 文本不得出现用户值");
        assertEquals(Collections.singletonList(injection), compiled.getParams());
    }

    @Test
    public void testIllegalLogicRejected() {
        assertThrows(ServiceException.class,
            () -> ConditionTreeCompiler.compile(group("xor", leaf("box_code", "eq", "A1")), whitelist));
    }

    @Test
    public void testValueValidation() {
        // IN 值全空白拒绝
        assertThrows(ServiceException.class,
            () -> ConditionTreeCompiler.compile(leaf("box_code", "in", " , ; "), whitelist));
        // BETWEEN 值数量必须为 2
        assertThrows(ServiceException.class,
            () -> ConditionTreeCompiler.compile(leaf("qty", "between", "1,2,3"), whitelist));
        // EQ 值为 null 拒绝
        assertThrows(ServiceException.class,
            () -> ConditionTreeCompiler.compile(leaf("box_code", "eq", null), whitelist));
    }

    @Test
    public void testToMybatisFragment() {
        CompiledCondition compiled = ConditionTreeCompiler.compile(
            group("or", leaf("box_code", "eq", "A1"), leaf("qty", "le", 99)), whitelist);
        assertEquals("(b.box_code = ?) OR (qty <= ?)", compiled.getFragment());
        // 纯 Map 键跳转 #{ctP.pN}（MapWrapper 不支持 List 下标）
        assertEquals("(b.box_code = #{ctP.p0}) OR (qty <= #{ctP.p1})",
            compiled.toMybatisFragment("ctP"));
        Map<String, Object> bound = compiled.toMybatisParams();
        assertEquals(2, bound.size());
        assertEquals("A1", bound.get("p0"));
        assertEquals(99, bound.get("p1"));
    }

    @Test
    public void testToMybatisFragmentPlaceholderMismatch() {
        CompiledCondition broken = new CompiledCondition("a = ?", new ArrayList<Object>());
        ServiceException ex = assertThrows(ServiceException.class, () -> broken.toMybatisFragment("ctP"));
        assertTrue(ex.getMessage().contains("占位符数量"));
    }
}
