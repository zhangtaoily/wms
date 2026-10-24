package com.wms.service.common.service.plugin;

import com.wms.common.api.BasicBO;
import com.wms.common.utils.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 表单验证插件 - 示例实现
 */
@Component
public class FormValidationPlugin extends AbstractPlugin {

    public FormValidationPlugin() {
        super("FormValidationPlugin", "form");
        setOrder(10); // 设置较高优先级
    }

    @Override
    protected boolean doExecute(PluginContext context) {
        // 只处理表单相关的操作
        if (!"form".equals(context.getPluginType())) {
            return true;
        }

        String operation = context.getOperation();

        // 只处理保存前的验证
        if ("beforeSave".equals(operation)) {
            BasicBO bo = context.getData();
            if (bo != null) {
                return validateForm(bo, context);
            }
        }

        return true;
    }

    /**
     * 表单验证逻辑
     */
    private boolean validateForm(BasicBO bo, PluginContext context) {
        // 示例：验证必填字段
        if (bo.getData() != null) {
            // 检查code字段是否为空
            Object code = bo.getData().get("code");
            if (code == null || StringUtils.isEmpty(code.toString())) {
                context.setAttribute("errorMessage", "编码不能为空");
                return false; // 验证失败，中断执行
            }

            // 检查name字段是否为空
            Object name = bo.getData().get("name");
            if (name == null || StringUtils.isEmpty(name.toString())) {
                context.setAttribute("errorMessage", "名称不能为空");
                return false; // 验证失败，中断执行
            }
        }

        return true; // 验证通过，继续执行后续插件
    }
}
