package com.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色功能权限 sys_role_module（角色设置-权限树勾选）
 */
@Data
@TableName("sys_role_module")
public class SysRoleModule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID（sys_role.id）
     */
    private Long roleId;

    /**
     * 功能模块ID（sys_func_module.id）
     */
    private Long moduleId;

    /**
     * 查看权限（1=有 0=无）
     */
    private Integer canView;

    /**
     * 编辑权限（1=有 0=无）
     */
    private Integer canEdit;

    private LocalDateTime createTime;
}
