package com.wms.service.common.service.plugin;

import com.wms.domain.SysFieldConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件上下文 - 封装插件执行时的上下文信息
 */
public class PluginContext {

    private final String pluginType;
    private final String operation;
    private Object data;
    private Map<String, Object> attributes;

    public PluginContext(String pluginType, String operation, Object data) {
        this.pluginType = pluginType;
        this.operation = operation;
        this.data = data;
        this.attributes = new HashMap<>();
    }

    /**
     * 使用枚举创建插件上下文
     */
    public PluginContext(PluginOperation operation, Object data) {
        this(operation.getPluginType(), operation.getOperation(), data);
    }

    public String getPluginType() {
        return pluginType;
    }

    public String getOperation() {
        return operation;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return type.isInstance(value) ? (T) value : null;
    }

    /**
     * 设置列表处理参数
     */
    public PluginContext withListParams(Map<String, Object> entries, Map<String, ?> columnConfigMap) {
        setAttribute("entries", entries);
        setAttribute("columnConfigMap", columnConfigMap);
        return this;
    }

    /**
     * 获取列表处理参数
     */
    public Map<String, Object> getEntries() {
        return getAttribute("entries", Map.class);
    }

    public Map<String, SysFieldConfig> getColumnConfigMap() {
        return getAttribute("columnConfigMap", Map.class);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * 创建表单插件上下文
     */
    public static PluginContext createFormContext(String operation, Object data) {
        return new PluginContext("form", operation, data);
    }

    /**
     * 创建列表插件上下文
     */
    public static PluginContext createListContext(String operation, Object data) {
        return new PluginContext("list", operation, data);
    }
}
