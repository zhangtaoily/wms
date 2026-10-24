package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.util.Date;

/**
 * 业务字典
 */
@Data
@TableName("base_dict")
public class BaseDict extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 字典编码（如 InType/OutType/Unit） */
    private String dictCode;

    /** 字典名称 */
    private String dictName;

    /** 备注 */
    private String remark;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
