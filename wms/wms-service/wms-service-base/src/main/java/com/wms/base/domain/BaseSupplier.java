package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.util.Date;

/**
 * 供应商档案
 */
@Data
@TableName("base_supplier")
public class BaseSupplier extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 供应商编码 */
    private String supplierCode;

    /** 供应商名称 */
    private String supplierName;

    /** 是否启用（1=启用 0=停用） */
    private Integer enabled;

    /** 最近一次与ERP同步时间 */
    private Date lastSync;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
