package com.wms.service.common.service.plugin;

import com.wms.common.utils.ListUtils;
import com.wms.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数值列合计行插件（页级）：对当前页全数值可解析的列求和，产出 attribute "sumRow"，
 * 经 PluginFacade.sumRow → DynamicsTableDataInfo.sumRow 返回前端
 *
 * @author wms
 */
@Component
public class SumRowPlugin extends AbstractPlugin {

    public SumRowPlugin() {
        super("SumRowPlugin", "list");
        setOrder(50);
    }

    @Override
    protected boolean doExecute(PluginContext context) {
        if (!PluginOperation.LIST_SUM_ROW.getOperation().equals(context.getOperation())) {
            return true;
        }
        List<Map<String, Object>> rows = context.getData();
        if (ListUtils.isEmpty(rows)) {
            return true;
        }
        Map<String, Object> sumRow = new LinkedHashMap<>();
        // 以首行为列基线（列键由 sys_field_config 驱动）
        for (String column : rows.get(0).keySet()) {
            if ("id".equals(column)) {
                continue;
            }
            BigDecimal sum = null;
            boolean numeric = true;
            for (Map<String, Object> row : rows) {
                Object value = row.get(column);
                if (value == null || StringUtils.isBlank(String.valueOf(value))) {
                    continue;
                }
                try {
                    BigDecimal decimal = new BigDecimal(String.valueOf(value));
                    sum = sum == null ? decimal : sum.add(decimal);
                } catch (NumberFormatException e) {
                    numeric = false;
                    break;
                }
            }
            if (numeric && sum != null) {
                sumRow.put(column, sum.stripTrailingZeros().toPlainString());
            }
        }
        context.setAttribute("sumRow", sumRow);
        return true;
    }
}
