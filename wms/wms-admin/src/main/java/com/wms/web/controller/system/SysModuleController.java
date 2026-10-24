package com.wms.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.wms.common.core.controller.BaseController;
import com.wms.common.core.domain.R;
import com.wms.system.service.SysModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能模块（两级权限树）
 *
 * @author wms
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/module")
public class SysModuleController extends BaseController {

    private final SysModuleService moduleService;

    /**
     * 获取模块权限树（组别→模块，角色设置勾选树数据源）
     */
    @SaCheckPermission("SYS_Role")
    @GetMapping("/treeselect")
    public R<List<Map<String, Object>>> treeselect() {
        return R.ok(moduleService.buildModuleTree());
    }

    /**
     * 按角色加载模块权限树及已勾选模块ID
     */
    @SaCheckPermission("SYS_Role")
    @GetMapping("/roleModuleTreeselect/{roleId}")
    public R<Map<String, Object>> roleModuleTreeselect(@PathVariable Long roleId) {
        Map<String, Object> ajax = new HashMap<>();
        ajax.put("modules", moduleService.buildModuleTree());
        ajax.put("checkedKeys", moduleService.selectCheckedModuleIdsByRoleId(roleId));
        return R.ok(ajax);
    }
}
