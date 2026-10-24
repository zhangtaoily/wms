package com.wms.service.common.service.plugin;

import com.wms.domain.SysFieldConfig;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 字段排序插件 - 处理字段排序逻辑
 */
@Component
public class FieldSortPlugin extends AbstractPlugin {

    public FieldSortPlugin() {
        super("FieldSortPlugin", "list");
        setOrder(5); // 设置较高优先级（在格式化之前执行）
    }

    @Override
    protected boolean doExecute(PluginContext context) {
        // 只处理列表相关的操作
        if (!"list".equals(context.getPluginType())) {
            return true;
        }

        String operation = context.getOperation();

        if (PluginOperation.LIST_PROCESS_ROW.getOperation().equals(operation)) {
            // 处理数据行时的排序处理
            return handleProcessRow(context);
        }

        return true;
    }

    /**
     * 处理数据行时的排序逻辑
     */
    private boolean handleProcessRow(PluginContext context) {
        Map<String, Object> entries = context.getEntries();
        Map<String, SysFieldConfig> columnConfigMap = context.getColumnConfigMap();

        if (entries != null && columnConfigMap != null) {
            // 创建基于字段配置的排序器
            Comparator<String> comparator = createFieldComparator(columnConfigMap);

            // 创建排序后的TreeMap并填充数据
            TreeMap<String, Object> sortedTreeMap = new TreeMap<>(comparator);

            // 遍历原始数据，按排序顺序放入TreeMap
            for (Map.Entry<String, Object> entry : entries.entrySet()) {
                sortedTreeMap.put(entry.getKey(), entry.getValue());
            }

            // 将处理后的TreeMap设置到上下文中
            context.setAttribute("processedTreeMap", sortedTreeMap);
        }

        return true;
    }

    /**
     * 创建基于字段配置的排序器。
     * 未配置列（如行主键 id 与插件下划线标记键）统一排在已配置列之后，
     * 同 rank 时按字段名打破平局——原实现同 rank 比较为 0，
     * TreeMap 会把 id 与 _docStatus/_syncRetryable 等键互相覆盖，
     * 导致前端列表行拿不到主键（BUG-10 根因）。
     */
    private Comparator<String> createFieldComparator(Map<String, SysFieldConfig> columnConfigMap) {
        Comparator<String> byRank = Comparator.comparingInt((String fieldName) -> {
            SysFieldConfig config = columnConfigMap.get(fieldName);
            return config != null ? config.getSortIndex() : Integer.MAX_VALUE;
        });
        return byRank.thenComparing(Comparator.naturalOrder());
    }

    /**
     * 创建已排序的TreeMap
     */
    public static TreeMap<String, Object> createSortedTreeMap(PluginContext context) {
        Comparator<String> comparator = context.getAttribute("fieldComparator", Comparator.class);

        if (comparator != null) {
            return new TreeMap<>(comparator);
        } else {
            // 如果没有提供排序器，使用默认排序
            return new TreeMap<>();
        }
    }
}
