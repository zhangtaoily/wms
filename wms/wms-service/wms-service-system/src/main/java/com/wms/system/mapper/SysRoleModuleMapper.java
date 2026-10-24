package com.wms.system.mapper;

import com.wms.common.core.mapper.BaseMapperPlus;
import com.wms.domain.SysFuncModule;
import com.wms.domain.SysRoleModule;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色功能权限 数据层
 */
public interface SysRoleModuleMapper extends BaseMapperPlus<SysRoleModuleMapper, SysRoleModule, SysRoleModule> {

    /**
     * 查询角色勾选的功能模块（权限树回显）
     */
    @Select("SELECT m.* FROM sys_func_module m " +
        "INNER JOIN sys_role_module rm ON rm.module_id = m.id " +
        "WHERE rm.role_id = #{roleId}")
    List<SysFuncModule> selectModulesByRoleId(Long roleId);
}
