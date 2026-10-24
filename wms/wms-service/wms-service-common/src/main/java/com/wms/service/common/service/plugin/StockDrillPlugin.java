package com.wms.service.common.service.plugin;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 库存下钻插件：行内存在箱码/托盘号时标记 _drillable，前端渲染库存四级下钻入口
 *
 * @author wms
 */
@Component
public class StockDrillPlugin extends AbstractPlugin {

    public StockDrillPlugin() {
        super("StockDrillPlugin", "list");
        setOrder(60);
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
            obtainRowTreeMap(context).put("_drillable", Boolean.TRUE);
        }
        return true;
    }
}
