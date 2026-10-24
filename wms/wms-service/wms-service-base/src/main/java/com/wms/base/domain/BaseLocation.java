package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.util.Date;

/**
 * 仓位信息
 */
@Data
@TableName("base_location")
public class BaseLocation extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 所属仓库ID（base_warehouse.id） */
    private Long warehouseId;

    /** ERP仓位编码 */
    private String erpLoc;

    /** 仓位代码（立库为 巷道-列-排-层 如 3-2-8-1，Y列如 Y-066） */
    private String locCode;

    /** 仓位名称 */
    private String locName;

    /** 外文名称 */
    private String outerName;

    /** 是否启用（1=启用 0=停用） */
    private Integer enabled;

    /** 最近一次与ERP同步时间 */
    private Date lastSync;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
