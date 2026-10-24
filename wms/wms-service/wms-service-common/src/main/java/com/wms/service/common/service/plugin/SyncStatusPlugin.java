package com.wms.service.common.service.plugin;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

/**
 * 同步状态插件：sync_status 翻译为可读文案，同步失败行标记 _syncRetryable 供前端渲染重试入口
 *
 * @author wms
 */
@Component
public class SyncStatusPlugin extends AbstractPlugin {

    public SyncStatusPlugin() {
        super("SyncStatusPlugin", "list");
        setOrder(40);
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
        Object raw = entries.get("sync_status");
        if (raw == null) {
            return true;
        }
        String status = String.valueOf(raw);
        String label;
        switch (status) {
            case "0":
                label = "待同步";
                break;
            case "1":
                label = "已同步";
                break;
            case "2":
                label = "同步失败";
                break;
            default:
                label = status;
                break;
        }
        TreeMap<String, Object> treeMap = obtainRowTreeMap(context);
        treeMap.put("sync_status", label);
        treeMap.put("_syncRetryable", "2".equals(status));
        return true;
    }
}
