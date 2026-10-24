package com.wms.service.common.service.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 插件外观服务类 - 负责具体的插件业务逻辑调用
 * 职责：提供简洁的插件调用接口，封装具体的业务逻辑
 * 设计模式：Facade Pattern（外观模式），为复杂的 PluginRegistry 提供简化的统一接口
 */
@Service
public class PluginFacade {

    @Autowired
    private PluginRegistry pluginRegistry;

    /**
     * 处理数据行的简洁调用方式
     * @param entries 数据行
     * @param columnConfigMap 字段配置
     * @return 处理后的TreeMap
     */
    public TreeMap<String, Object> processRow(Map<String, Object> entries, Map<String, ?> columnConfigMap) {
        PluginContext context = new PluginContext(PluginOperation.LIST_PROCESS_ROW, entries)
            .withListParams(entries, columnConfigMap);

        pluginRegistry.executePlugins(context);

        // 从上下文中获取处理结果
        TreeMap<String, Object> result = context.getAttribute("processedTreeMap", TreeMap.class);
        return result != null ? result : new TreeMap<>();
    }

    /**
     * 数值列合计行处理（SumRowPlugin 消费）
     * @param rows 当前页原始行
     * @param columnConfigMap 字段配置
     * @return 合计行，无插件产出时为 null
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> sumRow(List<Map<String, Object>> rows, Map<String, ?> columnConfigMap) {
        PluginContext context = new PluginContext(PluginOperation.LIST_SUM_ROW, rows)
            .withListParams(null, columnConfigMap);

        pluginRegistry.executePlugins(context);

        return context.getAttribute("sumRow", Map.class);
    }

    /**
     * 表单保存前处理
     * @param formData 表单数据
     * @return 处理后的表单数据
     */
    public Object processFormBeforeSave(Object formData) {
        PluginContext context = pluginRegistry.executePlugins(PluginOperation.FORM_BEFORE_SAVE, formData);
        return context.getData();
    }

    /**
     * 表单保存后处理
     * @param formData 表单数据
     * @return 处理后的表单数据
     */
    public Object processFormAfterSave(Object formData) {
        PluginContext context = pluginRegistry.executePlugins(PluginOperation.FORM_AFTER_SAVE, formData);
        return context.getData();
    }

    /**
     * 查询前处理
     * @param queryParams 查询参数
     * @return 处理后的查询参数
     */
    public Object processBeforeQuery(Object queryParams) {
        PluginContext context = pluginRegistry.executePlugins(PluginOperation.FORM_BEFORE_QUERY, queryParams);
        return context.getData();
    }

    /**
     * 查询后处理
     * @param queryResult 查询结果
     * @return 处理后的查询结果
     */
    public Object processAfterQuery(Object queryResult) {
        PluginContext context = pluginRegistry.executePlugins(PluginOperation.FORM_AFTER_QUERY, queryResult);
        return context.getData();
    }

    /**
     * 字段格式化处理
     * @param fieldName 字段名
     * @param value 字段值
     * @param fieldConfig 字段配置
     * @return 格式化后的值
     */
    public Object formatField(String fieldName, Object value, Object fieldConfig) {
        PluginContext context = new PluginContext(PluginOperation.LIST_FIELD_FORMAT, value);
        context.setAttribute("fieldName", fieldName);
        context.setAttribute("fieldConfig", fieldConfig);

        pluginRegistry.executePlugins(context);

        Object formattedValue = context.getAttribute("formattedValue");
        return formattedValue != null ? formattedValue : value;
    }
}
