package com.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 功能模块 sys_func_module（功能模块注册表，权限树数据源）
 */
@Data
@TableName("sys_func_module")
public class SysFuncModule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属组别ID（sys_module_group.id）
     */
    private Long groupId;

    /**
     * 模块编码（唯一，如 SYS_User/WMS_In）
     */
    private String moduleCode;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 模块类名
     */
    private String moduleCls;

    /**
     * 组织ID
     */
    private Long orgId;

    /**
     * 模块类型（列表/报表/终端）
     */
    private String moduleType;

    /**
     * 主表名
     */
    private String mainTable;

    /**
     * 状态字段名
     */
    private String statusField;

    /**
     * 是否启用（1=启用 0=停用）
     */
    private Integer enabled;

    /**
     * 是否挂接流程（1=是 0=否）
     */
    private Integer hasFlow;

    /**
     * 是否记录日志（1=是 0=否）
     */
    private Integer hasLog;

    /**
     * 模块路径（前端路由路径，如 /system/user）
     */
    private String modulePath;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sortNo;

    private String creator;

    private LocalDateTime createTime;

    private String updater;

    private LocalDateTime updateTime;
}
