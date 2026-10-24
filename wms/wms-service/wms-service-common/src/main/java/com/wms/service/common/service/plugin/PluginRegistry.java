package com.wms.service.common.service.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 插件注册器 - 负责插件的注册、管理和执行
 */
@Component
public class PluginRegistry {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<String, List<Plugin>> pluginsByType = new HashMap<>();

    /**
     * 初始化插件注册器
     */
    @PostConstruct
    public void init() {
        // 自动发现并注册所有插件
        registerPlugins();
        // 按优先级排序插件
        sortPlugins();
    }

    /**
     * 注册插件
     */
    private void registerPlugins() {
        Map<String, Plugin> pluginMap = applicationContext.getBeansOfType(Plugin.class);

        for (Plugin plugin : pluginMap.values()) {
            if (plugin.isEnabled()) {
                String pluginType = plugin.getType();
                pluginsByType.computeIfAbsent(pluginType, k -> new ArrayList<>()).add(plugin);
            }
        }
    }

    /**
     * 按优先级排序插件
     */
    private void sortPlugins() {
        for (List<Plugin> plugins : pluginsByType.values()) {
            plugins.sort(Comparator.comparingInt(Plugin::getOrder));
        }
    }

    /**
     * 获取指定类型的所有插件
     */
    public List<Plugin> getPlugins(String pluginType) {
        return pluginsByType.getOrDefault(pluginType, Collections.emptyList());
    }

    /**
     * 获取所有插件类型
     */
    public Set<String> getPluginTypes() {
        return pluginsByType.keySet();
    }

    /**
     * 执行插件链
     * @param context 插件上下文
     * @return 是否继续执行（如果某个插件返回false，则中断执行）
     */
    public boolean executePlugins(PluginContext context) {
        String pluginType = context.getPluginType();
        List<Plugin> plugins = getPlugins(pluginType);

        for (Plugin plugin : plugins) {
            if (!plugin.execute(context)) {
                // 插件要求中断执行
                return false;
            }
        }

        return true;
    }

    /**
     * 基于枚举执行插件链
     * @param operation 插件操作枚举
     * @param data 主要数据
     * @return 插件上下文，包含处理结果
     */
    public PluginContext executePlugins(PluginOperation operation, Object data) {
        PluginContext context = new PluginContext(operation, data);
        executePlugins(context);
        return context;
    }



    /**
     * 手动注册插件
     */
    public void registerPlugin(Plugin plugin) {
        if (plugin.isEnabled()) {
            String pluginType = plugin.getType();
            List<Plugin> plugins = pluginsByType.computeIfAbsent(pluginType, k -> new ArrayList<>());

            if (!plugins.contains(plugin)) {
                plugins.add(plugin);
                sortPlugins();
            }
        }
    }

    /**
     * 移除插件
     */
    public void unregisterPlugin(Plugin plugin) {
        String pluginType = plugin.getType();
        List<Plugin> plugins = pluginsByType.get(pluginType);
        if (plugins != null) {
            plugins.remove(plugin);
        }
    }

    /**
     * 根据名称查找插件
     */
    public Plugin getPluginByName(String name) {
        return pluginsByType.values().stream()
            .flatMap(List::stream)
            .filter(p -> p.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}
