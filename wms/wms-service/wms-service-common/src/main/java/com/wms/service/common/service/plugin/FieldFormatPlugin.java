package com.wms.service.common.service.plugin;

import com.wms.common.format.FormatManager;
import com.wms.common.format.FormatterEnum;
import com.wms.common.format.IFormatter;
import com.wms.common.utils.StringUtils;
import com.wms.domain.SysFieldConfig;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * 字段格式化插件 - 示例实现
 */
@Component
public class FieldFormatPlugin extends AbstractPlugin {

    public FieldFormatPlugin() {
        super("FieldFormatPlugin", "list");
        setOrder(20); // 设置优先级
    }

    @Override
    protected boolean doExecute(PluginContext context) {
        // 只处理列表相关的操作
        if (!"list".equals(context.getPluginType())) {
            return true;
        }

        String operation = context.getOperation();

        if (PluginOperation.LIST_PROCESS_ROW.getOperation().equals(operation)) {
            // 处理数据行时的格式化处理
            return handleProcessRow(context);
        }

        return true;
    }

    /**
     * 处理数据行时的格式化处理
     */
    private boolean handleProcessRow(PluginContext context) {
        Map<String, Object> entries = context.getEntries();
        Map<String, ?> columnConfigMap = context.getColumnConfigMap();

        if (entries == null || columnConfigMap == null) {
            return true;
        }

        // 获取已排序的TreeMap（由FieldSortPlugin处理）
        TreeMap<String, Object> processedTreeMap = context.getAttribute("processedTreeMap", TreeMap.class);
        TreeMap<String, Object> treeMap = processedTreeMap != null ? processedTreeMap : new TreeMap<>();

        // 遍历所有字段进行格式化
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            // 获取字段配置
            Object fieldConfigObj = columnConfigMap.get(fieldName);
            if (fieldConfigObj instanceof SysFieldConfig) {
                SysFieldConfig fieldConfig = (SysFieldConfig) fieldConfigObj;

                // 格式化字段值
                Object formattedValue = formatValue(value, fieldConfig);
                treeMap.put(fieldName, formattedValue);
            } else {
                // 没有配置的字段，保持原值
                treeMap.put(fieldName, value);
            }
        }

        // 将格式化后的TreeMap设置到上下文中
        context.setAttribute("processedTreeMap", treeMap);

        return true;
    }

    /**
     * 根据字段配置格式化值
     */
    private Object formatValue(Object value, SysFieldConfig fieldConfig) {
        if (value == null || fieldConfig == null) {
            return value;
        }

        // 根据字段配置进行格式化
        String format = fieldConfig.getFormat();
        if (StringUtils.isEmpty(format)) {
            return value;
        }

        try {
            return formatValue(value, format);
        } catch (Exception e) {
            // 格式化失败，保持原值
            return value;
        }
    }

    /**
     * 处理批量数据格式化
     */
    private boolean handleBatchFormat(PluginContext context) {
        TreeMap<String, Object> data = context.getData();
        Map<String, SysFieldConfig> fieldConfigs = context.getAttribute("fieldConfigs", Map.class);

        if (data != null && fieldConfigs != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                SysFieldConfig fieldConfig = fieldConfigs.get(fieldName);

                if (fieldConfig != null && StringUtils.isNotEmpty(fieldConfig.getFormat())) {
                    Object formattedValue = formatValue(value, fieldConfig.getFormat());
                    if (formattedValue != null) {
                        data.put(fieldName, formattedValue);
                    }
                }
            }
        }

        return true;
    }

    /**
     * 根据格式字符串格式化值
     */
    private Object formatValue(Object value, String format) {
        if (value == null) {
            return null;
        }

        try {
            FormatterEnum formatterEnum = FormatterEnum.formatorEnumMap.get(format);
            IFormatter formatter = FormatManager.getInstance().getFormat(formatterEnum);
            if (formatter != null) {
                value = formatter.format(value);
            }
            return value;
        } catch (Exception e) {
            return value;
        }
    }
}
