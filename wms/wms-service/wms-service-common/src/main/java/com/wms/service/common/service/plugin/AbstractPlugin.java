package com.wms.service.common.service.plugin;

import java.util.Map;
import java.util.TreeMap;

/**
 * 抽象插件基类 - 提供默认实现
 */
public abstract class AbstractPlugin implements Plugin {

    protected String name;
    protected String type;
    protected boolean enabled = true;
    protected int order = 100;

    public AbstractPlugin(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * 获取（或初始化）行处理结果 TreeMap：行级插件按 order 依次共享
     */
    protected TreeMap<String, Object> obtainRowTreeMap(PluginContext context) {
        TreeMap<String, Object> treeMap = context.getAttribute("processedTreeMap", TreeMap.class);
        if (treeMap == null) {
            treeMap = new TreeMap<>();
            Map<String, Object> entries = context.getEntries();
            if (entries != null) {
                treeMap.putAll(entries);
            }
            context.setAttribute("processedTreeMap", treeMap);
        }
        return treeMap;
    }

    /**
     * 默认执行逻辑：如果启用则执行具体逻辑
     */
    @Override
    public boolean execute(PluginContext context) {
        if (!isEnabled()) {
            return true; // 插件未启用，继续执行后续插件
        }

        return doExecute(context);
    }

    /**
     * 具体执行逻辑 - 由子类实现
     * @param context 插件上下文
     * @return 是否继续执行后续插件
     */
    protected abstract boolean doExecute(PluginContext context);
}
