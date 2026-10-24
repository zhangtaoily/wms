package com.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模块组别 sys_module_group（左侧菜单分组）
 */
@Data
@TableName("sys_module_group")
public class SysModuleGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 组别编码（WMOperate/ASRSOperate/RMOperate/MDB/SystemSetting）
     */
    private String groupCode;

    /**
     * 组别名称
     */
    private String groupName;

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
