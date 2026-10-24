package com.wms.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.constant.UserConstants;
import com.wms.common.helper.LoginHelper;
import com.wms.common.utils.StringUtils;
import com.wms.domain.SysFuncModule;
import com.wms.domain.SysModuleGroup;
import com.wms.domain.SysRoleModule;
import com.wms.service.common.vo.MetaVo;
import com.wms.service.common.vo.RouterVo;
import com.wms.system.mapper.SysFuncModuleMapper;
import com.wms.system.mapper.SysModuleGroupMapper;
import com.wms.system.mapper.SysRoleModuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 两级权限服务：sys_module_group（组别）→ sys_func_module（模块）
 * getRouters 菜单树组装与角色模块过滤
 */
@RequiredArgsConstructor
@Service
public class SysModuleService {

    private final SysModuleGroupMapper moduleGroupMapper;
    private final SysFuncModuleMapper funcModuleMapper;
    private final SysRoleModuleMapper roleModuleMapper;

    /**
     * 按用户组装两级菜单路由树（组别 → 模块）
     * admin 返回全部启用模块；普通用户按 sys_role_module.can_view 过滤
     */
    public List<RouterVo> buildRoutersByUserId(Long userId) {
        List<SysModuleGroup> groups = moduleGroupMapper.selectList(
            new LambdaQueryWrapper<SysModuleGroup>().orderByAsc(SysModuleGroup::getSortNo));
        List<SysFuncModule> modules = listModulesByUserId(userId);

        Map<Long, List<SysFuncModule>> groupModules = modules.stream()
            .collect(Collectors.groupingBy(SysFuncModule::getGroupId, LinkedHashMap::new, Collectors.toList()));

        List<RouterVo> routers = new ArrayList<>();
        for (SysModuleGroup group : groups) {
            List<SysFuncModule> children = groupModules.get(group.getId());
            if (children == null || children.isEmpty()) {
                continue;
            }
            RouterVo router = new RouterVo();
            router.setName(group.getGroupCode());
            router.setPath("/" + group.getGroupCode());
            router.setHidden(false);
            router.setAlwaysShow(true);
            router.setRedirect("noRedirect");
            router.setComponent(UserConstants.LAYOUT);
            router.setMeta(new MetaVo(group.getGroupName(), group.getIcon()));
            List<RouterVo> moduleRouters = new ArrayList<>();
            for (SysFuncModule m : children) {
                moduleRouters.add(toModuleRouter(m));
                // 动态列表模块追加同级 hidden 表单路由，供 DynamicList openPage 以 path + "/form" 打开页签
                String mp = StringUtils.stripStart(m.getModulePath(), "/");
                if (mp.startsWith("demo/") && !"demo/dynamic-list-demo".equals(mp)) {
                    moduleRouters.add(toFormRouter(m));
                }
            }
            router.setChildren(moduleRouters);
            routers.add(router);
        }
        return routers;
    }

    /**
     * 用户可见模块权限字符集（module_code），供 getInfo permissions 输出
     */
    public Set<String> selectModulePermsByUserId(Long userId) {
        return listModulesByUserId(userId).stream()
            .map(SysFuncModule::getModuleCode)
            .collect(Collectors.toSet());
    }

    /**
     * 角色勾选的功能模块列表（角色设置-权限树回显，m1-9 角色页面使用）
     */
    public List<SysFuncModule> selectModulesByRoleId(Long roleId) {
        return roleModuleMapper.selectModulesByRoleId(roleId);
    }

    /**
     * 两级权限树（角色设置-模块权限勾选树数据源）
     * 组节点 id="g{groupId}"，模块节点 id=moduleId（数字）
     */
    public List<Map<String, Object>> buildModuleTree() {
        List<SysModuleGroup> groups = moduleGroupMapper.selectList(
            new LambdaQueryWrapper<SysModuleGroup>().orderByAsc(SysModuleGroup::getSortNo));
        List<SysFuncModule> modules = funcModuleMapper.selectList(new LambdaQueryWrapper<SysFuncModule>()
            .eq(SysFuncModule::getEnabled, 1)
            .orderByAsc(SysFuncModule::getSortNo));

        Map<Long, List<SysFuncModule>> groupModules = modules.stream()
            .collect(Collectors.groupingBy(SysFuncModule::getGroupId, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> tree = new ArrayList<>();
        for (SysModuleGroup group : groups) {
            List<SysFuncModule> children = groupModules.get(group.getId());
            if (children == null || children.isEmpty()) {
                continue;
            }
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", "g" + group.getId());
            node.put("label", group.getGroupName());
            node.put("children", children.stream().map(m -> {
                Map<String, Object> child = new LinkedHashMap<>();
                child.put("id", m.getId());
                child.put("label", m.getModuleName());
                return child;
            }).collect(Collectors.toList()));
            tree.add(node);
        }
        return tree;
    }

    /**
     * 角色已勾选的模块ID集合（权限树回显）
     */
    public List<Long> selectCheckedModuleIdsByRoleId(Long roleId) {
        return roleModuleMapper.selectList(new LambdaQueryWrapper<SysRoleModule>()
                .eq(SysRoleModule::getRoleId, roleId))
            .stream().map(SysRoleModule::getModuleId).collect(Collectors.toList());
    }

    private List<SysFuncModule> listModulesByUserId(Long userId) {
        if (LoginHelper.isAdmin(userId)) {
            return funcModuleMapper.selectList(new LambdaQueryWrapper<SysFuncModule>()
                .eq(SysFuncModule::getEnabled, 1)
                .orderByAsc(SysFuncModule::getSortNo));
        }
        return funcModuleMapper.selectVisibleModulesByUserId(userId);
    }

    private RouterVo toModuleRouter(SysFuncModule module) {
        RouterVo router = new RouterVo();
        router.setName(module.getModuleCode());
        String path = StringUtils.stripStart(module.getModulePath(), "/");
        router.setPath(path);
        router.setHidden(false);
        // /demo/ 前缀的动态模块共用通用列表组件（path 各异避免 vue-router 冲突，
        // 前端 loadView 会按 route.path 生成唯一组件名解决 keep-alive 复用问题）
        if (path.startsWith("demo/") && !"demo/dynamic-list-demo".equals(path)) {
            router.setComponent("demo/dynamic-list-demo/index");
        } else {
            router.setComponent(path + "/index");
        }
        router.setMeta(new MetaVo(module.getModuleName(), module.getIcon(), false, null, module.getModuleCode()));
        return router;
    }

    /**
     * 动态列表模块配套表单路由（hidden、不缓存）
     * 完整路径 = /{groupCode}/{modulePath}/form，与 DynamicList openPage 拼接的 path + "/form" 匹配
     */
    private RouterVo toFormRouter(SysFuncModule module) {
        RouterVo router = new RouterVo();
        router.setName(module.getModuleCode() + "Form");
        String path = StringUtils.stripStart(module.getModulePath(), "/");
        router.setPath(path + "/form");
        router.setHidden(true);
        router.setComponent("demo/dynamic-form-demo/index");
        router.setMeta(new MetaVo(module.getModuleName(), module.getIcon(), true, null, module.getModuleCode()));
        return router;
    }
}
