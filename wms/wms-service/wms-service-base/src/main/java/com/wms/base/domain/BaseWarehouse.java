package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.util.Date;

/**
 * 仓库信息
 */
@Data
@TableName("base_warehouse")
public class BaseWarehouse extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 仓库编码 */
    private String warehouseNo;

    /** 仓库名称 */
    private String warehouseName;

    /** ERP仓库编码 */
    private String erpNo;

    /** ERP仓库名称 */
    private String erpName;

    /** 仓库类型（立库(自营)/普通仓库） */
    private String whType;

    /** 是否启用（1=启用 0=停用） */
    private Integer enabled;

    /** 最近一次与ERP同步时间 */
    private Date lastSync;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
