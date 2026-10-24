package com.wms.service.common.service.plugin;

import com.wms.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 单据状态插件：状态类列（列名含 status）收集为 _docStatus 标记，供前端 tag 着色
 *
 * @author wms
 */
@Component
public class DocStatusPlugin extends AbstractPlugin {

    public DocStatusPlugin() {
        super("DocStatusPlugin", "list");
        setOrder(25);
    }

    @Override
    protected boolean doExecute(PluginContext context) {
        if (!PluginOperation.LIST_PROCESS_ROW.getOperation().equals(context.getOperation())) {
            return true;
        }
        Map<String, Object> entries = context.getEntries();
        if (entries == null) {
            return true;
        }
        TreeMap<String, Object> treeMap = obtainRowTreeMap(context);
        Map<String, Object> docStatus = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            String column = entry.getKey();
            if (StringUtils.isNotEmpty(column) && column.toLowerCase().contains("status") && entry.getValue() != null) {
                docStatus.put(column, String.valueOf(entry.getValue()));
            }
        }
        if (!docStatus.isEmpty()) {
            treeMap.put("_docStatus", docStatus);
        }
        return true;
    }
}
