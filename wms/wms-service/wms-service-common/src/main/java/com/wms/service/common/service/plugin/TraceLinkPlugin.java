package com.wms.service.common.service.plugin;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 追溯链接插件：行内存在箱码/托盘号时标记 _linkType=trace，前端渲染为追溯链接
 *
 * @author wms
 */
@Component
public class TraceLinkPlugin extends AbstractPlugin {

    public TraceLinkPlugin() {
        super("TraceLinkPlugin", "list");
        setOrder(35);
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
        if (entries.get("box_code") != null || entries.get("pallet_no") != null) {
            obtainRowTreeMap(context).put("_linkType", "trace");
        }
        return true;
    }
}
