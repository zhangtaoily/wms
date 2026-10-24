package com.wms.service.common.service.plugin;

/**
 * 插件接口 - 所有插件的通用基接口
 */
public interface Plugin {

    /**
     * 获取插件名称
     */
    String getName();

    /**
     * 获取插件类型
     */
    String getType();

    /**
     * 插件是否启用
     */
    boolean isEnabled();

    /**
     * 设置插件启用状态
     */
    void setEnabled(boolean enabled);

    /**
     * 插件执行顺序（数值越小优先级越高）
     */
    int getOrder();

    /**
     * 执行插件逻辑
     * @param context 插件上下文
     * @return 是否继续执行后续插件（true: 继续, false: 中断）
     */
    boolean execute(PluginContext context);
}
