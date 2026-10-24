package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.util.Date;

/**
 * 打印模板
 */
@Data
@TableName("sys_print_template")
public class SysPrintTemplate extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 所属功能模块 */
    private String moduleName;

    /** 模板名称 */
    private String tplName;

    /** 模板内容 */
    private String tplContent;

    /** 是否启用（1=启用 0=停用） */
    private Integer enabled;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
