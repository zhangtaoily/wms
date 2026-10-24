package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.util.Date;

/**
 * 物料档案
 */
@Data
@TableName("base_material")
public class BaseMaterial extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 纱支 */
    private String yarn;

    /** 原料成分 */
    private String comp;

    /** ERP物料编码 */
    private String erpNo;

    /** 最近一次与ERP同步时间 */
    private Date lastSync;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
