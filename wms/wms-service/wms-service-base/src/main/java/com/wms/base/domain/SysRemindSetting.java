package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.util.Date;

/**
 * 提醒设置
 */
@Data
@TableName("sys_remind_setting")
public class SysRemindSetting extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 排序号 */
    private Integer sortNo;

    /** 所属功能模块 */
    private String moduleName;

    /** 提醒名称 */
    private String remindName;

    /** 主键字段（默认ID） */
    private String pkField;

    /** 是否公共提醒（1=是 0=否） */
    private Integer isPublic;

    /** 允许提醒的用户，逗号分隔 */
    private String allowUsers;

    /** 是否启用（1=启用 0=停用） */
    private Integer enabled;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
