package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.util.Date;

/**
 * 业务批次档案
 */
@Data
@TableName("base_biz_batch")
public class BaseBizBatch extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 业务批号 */
    private String bizBatchNo;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称（冗余） */
    private String materialName;

    /** 纱支（冗余） */
    private String yarn;

    /** 成分（冗余） */
    private String comp;

    /** 批次建立时间 */
    private Date bizDate;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
