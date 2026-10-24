package com.wms.system.service;

import com.wms.common.core.domain.entity.SysUser;
import com.wms.service.common.spi.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户权限处理
 *
 * @author ruoyi
 */
@RequiredArgsConstructor
@Service
public class SysPermissionService {

    private final ISysRoleService roleService;
    private final ISysMenuService menuService;
    private final SysModuleService moduleService;

    /**
     * 获取角色数据权限
     *
     * @param user 用户信息
     * @return 角色权限信息
     */
    public Set<String> getRolePermission(SysUser user) {
        Set<String> roles = new HashSet<>();
        // 管理员拥有所有权限
        if (user.isAdmin()) {
            roles.add("admin");
        } else {
            roles.addAll(roleService.selectRolePermissionByUserId(user.getId()));
        }
        return roles;
    }

    /**
     * 获取菜单数据权限
     *
     * @param user 用户信息
     * @return 菜单权限信息
     */
    public Set<String> getMenuPermission(SysUser user) {
        Set<String> perms = new HashSet<>();
        // 管理员拥有所有权限
        if (user.isAdmin()) {
            perms.add("*:*:*");
            // 并入功能模块权限字符（module_code）：SYS_* 为单段风格，通配符 *:*:* 按冒号分段无法覆盖
            perms.addAll(moduleService.selectModulePermsByUserId(user.getId()));
        } else {
            perms.addAll(menuService.selectMenuPermsByUserId(user.getId()));
            // 并入功能模块权限字符（module_code），两级权限树授权后模块级 @SaCheckPermission 生效
            perms.addAll(moduleService.selectModulePermsByUserId(user.getId()));
        }
        return perms;
    }
}
