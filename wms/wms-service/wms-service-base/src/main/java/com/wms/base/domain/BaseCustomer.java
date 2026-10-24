package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.util.Date;

/**
 * 客户档案
 */
@Data
@TableName("base_customer")
public class BaseCustomer extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 客户编码（ERP） */
    private String customerCode;

    /** 客户名称 */
    private String customerName;

    /** 是否启用（1=启用 0=停用） */
    private Integer enabled;

    /** 最近一次与ERP同步时间 */
    private Date lastSync;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
