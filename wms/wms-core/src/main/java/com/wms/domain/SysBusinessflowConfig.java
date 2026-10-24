package com.wms.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.BaseEntity;
import lombok.Data;

/**
 * (SysBusinessflowConfig)实体类
 *
 * @author makejava
 * @since 2024-08-15 13:29:59
 */
@Data
@TableName("sys_businessflow_config")
public class SysBusinessflowConfig extends BaseEntity {
    private static final long serialVersionUID = -65589177758200964L;


    private String modelId;

    private String sendType;

    private String workTiming;

    /**
     * 备注
     */
    private String remark;

}

