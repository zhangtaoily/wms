package com.wms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.domain.SysFuncModule;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 功能模块 数据层
 */
public interface SysFuncModuleMapper extends BaseMapper<SysFuncModule> {

    String COLUMNS = " m.id, m.group_id, m.module_code, m.module_name, m.module_cls, m.org_id, m.module_type, " +
        "m.main_table, m.status_field, m.enabled, m.has_flow, m.has_log, m.module_path, m.icon, m.sort_no";

    /**
     * 查询用户可见的启用模块（按角色勾选 sys_role_module.can_view）
     */
    @Select("SELECT " + COLUMNS + " FROM sys_func_module m " +
        "INNER JOIN sys_role_module rm ON rm.module_id = m.id AND rm.can_view = 1 " +
        "INNER JOIN sys_user_role ur ON ur.role_id = rm.role_id " +
        "WHERE ur.user_id = #{userId} AND m.enabled = 1 " +
        "ORDER BY m.sort_no, m.id")
    List<SysFuncModule> selectVisibleModulesByUserId(Long userId);
}
