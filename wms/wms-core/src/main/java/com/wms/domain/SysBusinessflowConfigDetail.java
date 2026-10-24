package com.wms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * (SysBusinessflowConfigDetail)实体类
 *
 * @author makejava
 * @since 2024-08-15 13:36:10
 */
@Data
@TableName("sys_businessflow_config_detail")
public class SysBusinessflowConfigDetail extends IdEntity {
    private static final long serialVersionUID = -14021905570342585L;


    private String orgSource;

    private Long orgId;

    private String systemId;

    private String purposeId;

    private String conditionSql;

    private int enabled;

    private int sortIndex;
    private Long businessId;
}

