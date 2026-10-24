package com.wms.service.common.service.plugin;

/**
 * 插件操作类型枚举 - 定义标准的插件操作
 */
public enum PluginOperation {

    // 列表相关操作
    LIST_PROCESS_ROW("list", "processRow", "处理数据行"),
    LIST_FIELD_FORMAT("list", "fieldFormat", "字段格式化"),
    LIST_CREATE_TREE_MAP("list", "createTreeMap", "创建排序TreeMap"),
    LIST_SUM_ROW("list", "sumRow", "数值列合计行"),

    // 表单相关操作
    FORM_BEFORE_SAVE("form", "beforeSave", "保存前处理"),
    FORM_AFTER_SAVE("form", "afterSave", "保存后处理"),
    FORM_BEFORE_QUERY("form", "beforeQuery", "查询前处理"),
    FORM_AFTER_QUERY("form", "afterQuery", "查询后处理");

    private final String pluginType;
    private final String operation;
    private final String description;

    PluginOperation(String pluginType, String operation, String description) {
        this.pluginType = pluginType;
        this.operation = operation;
        this.description = description;
    }

    public String getPluginType() {
        return pluginType;
    }

    public String getOperation() {
        return operation;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据插件类型和操作获取枚举
     */
    public static PluginOperation of(String pluginType, String operation) {
        for (PluginOperation op : values()) {
            if (op.pluginType.equals(pluginType) && op.operation.equals(operation)) {
                return op;
            }
        }
        return null;
    }
}
