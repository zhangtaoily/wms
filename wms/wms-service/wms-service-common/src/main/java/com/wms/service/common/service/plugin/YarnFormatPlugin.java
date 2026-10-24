package com.wms.service.common.service.plugin;

import com.wms.common.utils.StringUtils;
import com.wms.domain.SysFieldConfig;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

/**
 * 纱线格式化插件：处理 wms: 前缀格式键（wms:regain / wms:yarnCount / wms:standardWeight / wms:component），
 * 不动 FormatManager 既有 FormatterEnum 体系
 *
 * @author wms
 */
@Component
public class YarnFormatPlugin extends AbstractPlugin {

    private static final String PREFIX = "wms:";

    public YarnFormatPlugin() {
        super("YarnFormatPlugin", "list");
        setOrder(30);
    }

    @Override
    protected boolean doExecute(PluginContext context) {
        if (!PluginOperation.LIST_PROCESS_ROW.getOperation().equals(context.getOperation())) {
            return true;
        }
        Map<String, Object> entries = context.getEntries();
        Map<String, ?> columnConfigMap = context.getColumnConfigMap();
        if (entries == null || columnConfigMap == null) {
            return true;
        }
        TreeMap<String, Object> treeMap = obtainRowTreeMap(context);
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            Object fieldConfig = columnConfigMap.get(entry.getKey());
            if (!(fieldConfig instanceof SysFieldConfig) || entry.getValue() == null) {
                continue;
            }
            String format = ((SysFieldConfig) fieldConfig).getFormat();
            if (StringUtils.isEmpty(format) || !format.startsWith(PREFIX)) {
                continue;
            }
            treeMap.put(entry.getKey(), formatYarn(entry.getValue(), format.substring(PREFIX.length())));
        }
        return true;
    }

    private Object formatYarn(Object value, String kind) {
        String text = String.valueOf(value).trim();
        if ("regain".equals(kind)) {
            // 回潮率：数值去尾零 + %
            return stripZeros(text) + "%";
        }
        if ("yarnCount".equals(kind)) {
            // 纱支：数值去尾零 + S
            return stripZeros(text) + "S";
        }
        if ("standardWeight".equals(kind)) {
            // 标准重量：三位小数 + kg
            return decimals(text, 3) + "kg";
        }
        // component 等其余 wms: 键：原样
        return value;
    }

    private String stripZeros(String text) {
        try {
            return new BigDecimal(text).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return text;
        }
    }

    private String decimals(String text, int scale) {
        try {
            return new BigDecimal(text).setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString();
        } catch (NumberFormatException e) {
            return text;
        }
    }
}
