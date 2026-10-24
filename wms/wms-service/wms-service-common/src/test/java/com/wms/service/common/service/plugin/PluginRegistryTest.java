package com.wms.service.common.service.plugin;

import com.wms.domain.SysFieldConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 插件注册与执行回归单测（不走 Spring，手动 registerPlugin）：
 * 8 插件 order 排序、6 新插件 LIST_PROCESS_ROW / LIST_SUM_ROW 冒烟、中断与禁用语义
 */
public class PluginRegistryTest {

    @Test
    public void testEightPluginsSortedByOrder() {
        PluginRegistry registry = new PluginRegistry();
        // 乱序注册，验证按 order 升序重排
        registry.registerPlugin(new StockDrillPlugin());
        registry.registerPlugin(new DocStatusPlugin());
        registry.registerPlugin(new SumRowPlugin());
        registry.registerPlugin(new FieldSortPlugin());
        registry.registerPlugin(new SyncStatusPlugin());
        registry.registerPlugin(new YarnFormatPlugin());
        registry.registerPlugin(new FieldFormatPlugin());
        registry.registerPlugin(new TraceLinkPlugin());

        List<Plugin> plugins = registry.getPlugins("list");
        assertEquals(8, plugins.size());
        String[] expectedNames = {"FieldSortPlugin", "FieldFormatPlugin", "DocStatusPlugin", "YarnFormatPlugin",
            "TraceLinkPlugin", "SyncStatusPlugin", "SumRowPlugin", "StockDrillPlugin"};
        int[] expectedOrders = {5, 20, 25, 30, 35, 40, 50, 60};
        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(expectedNames[i], plugins.get(i).getName(), "第 " + i + " 位插件顺序不符");
            assertEquals(expectedOrders[i], plugins.get(i).getOrder(), "第 " + i + " 位插件 order 不符");
        }
    }

    @Test
    public void testRowPluginChainSmoke() {
        PluginRegistry registry = new PluginRegistry();
        registry.registerPlugin(new DocStatusPlugin());
        registry.registerPlugin(new YarnFormatPlugin());
        registry.registerPlugin(new TraceLinkPlugin());
        registry.registerPlugin(new SyncStatusPlugin());
        registry.registerPlugin(new StockDrillPlugin());

        SysFieldConfig regainConfig = new SysFieldConfig();
        regainConfig.setColumnName("regain");
        regainConfig.setFormat("wms:regain");
        Map<String, SysFieldConfig> columnConfigMap = new HashMap<>();
        columnConfigMap.put("regain", regainConfig);

        Map<String, Object> entries = new HashMap<>();
        entries.put("regain", "8.50");
        entries.put("sync_status", "2");
        entries.put("box_code", "BOX001");
        entries.put("remark", "abc");

        PluginContext context = new PluginContext(PluginOperation.LIST_PROCESS_ROW, entries)
            .withListParams(entries, columnConfigMap);
        assertTrue(registry.executePlugins(context));

        TreeMap<?, ?> treeMap = context.getAttribute("processedTreeMap", TreeMap.class);
        assertNotNull(treeMap);
        assertEquals("8.5%", treeMap.get("regain"), "YarnFormatPlugin 回潮率格式化");
        assertEquals("同步失败", treeMap.get("sync_status"), "SyncStatusPlugin 状态翻译");
        assertEquals(Boolean.TRUE, treeMap.get("_syncRetryable"), "失败行标记可重试");
        assertEquals("trace", treeMap.get("_linkType"), "TraceLinkPlugin 追溯链接");
        assertEquals(Boolean.TRUE, treeMap.get("_drillable"), "StockDrillPlugin 下钻标记");
        Object docStatus = treeMap.get("_docStatus");
        assertTrue(docStatus instanceof Map, "DocStatusPlugin 状态收集");
        assertEquals("2", ((Map<?, ?>) docStatus).get("sync_status"));
        // 无配置列保持原值
        assertEquals("abc", treeMap.get("remark"));
    }

    @Test
    public void testSumRowPluginSmoke() {
        PluginRegistry registry = new PluginRegistry();
        registry.registerPlugin(new SumRowPlugin());

        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("id", 1L);
        row1.put("qty", "10");
        row1.put("price", "2.50");
        row1.put("name", "a");
        Map<String, Object> row2 = new HashMap<>();
        row2.put("id", 2L);
        row2.put("qty", "5");
        row2.put("price", null);
        row2.put("name", "b");
        rows.add(row1);
        rows.add(row2);

        PluginContext context = new PluginContext(PluginOperation.LIST_SUM_ROW, rows)
            .withListParams(null, null);
        assertTrue(registry.executePlugins(context));

        Map<?, ?> sumRow = context.getAttribute("sumRow", Map.class);
        assertNotNull(sumRow);
        assertEquals("15", sumRow.get("qty"));
        assertEquals("2.5", sumRow.get("price"));
        assertFalse(sumRow.containsKey("name"), "文本列不求和");
        assertFalse(sumRow.containsKey("id"), "id 列跳过");
    }

    @Test
    public void testRowPluginsIgnoreOtherOperations() {
        PluginRegistry registry = new PluginRegistry();
        registry.registerPlugin(new DocStatusPlugin());
        registry.registerPlugin(new SyncStatusPlugin());
        registry.registerPlugin(new StockDrillPlugin());

        PluginContext context = new PluginContext(PluginOperation.LIST_SUM_ROW, new ArrayList<Map<String, Object>>())
            .withListParams(null, null);
        assertTrue(registry.executePlugins(context));
        assertNull(context.getAttribute("processedTreeMap", TreeMap.class), "行级插件不得响应非 processRow 操作");
        assertNull(context.getAttribute("sumRow", Map.class), "未注册 SumRowPlugin 时不得产出合计行");
    }

    @Test
    public void testExecutePluginsInterruptedByFalse() {
        PluginRegistry registry = new PluginRegistry();
        registry.registerPlugin(new AbstractPlugin("interrupt", "list") {
            @Override
            protected boolean doExecute(PluginContext context) {
                return false;
            }
        });
        PluginContext context = new PluginContext(PluginOperation.LIST_PROCESS_ROW, null);
        assertFalse(registry.executePlugins(context), "插件返回 false 时执行链应中断");
    }

    @Test
    public void testDisabledPluginNotRegistered() {
        PluginRegistry registry = new PluginRegistry();
        DocStatusPlugin plugin = new DocStatusPlugin();
        plugin.setEnabled(false);
        registry.registerPlugin(plugin);
        assertEquals(0, registry.getPlugins("list").size());
    }
}
