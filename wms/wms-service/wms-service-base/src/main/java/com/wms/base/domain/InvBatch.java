package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存批次（批次粒度总量台账）
 */
@Data
@TableName("inv_batch")
public class InvBatch extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 库存批号 */
    private String invBatchNo;

    /** 业务批号 */
    private String bizBatchNo;

    /** 缸号 */
    private String vatNo;

    /** 原料成分 */
    private String comp;

    /** 总箱数 */
    private Integer totalBoxes;

    /** 总件数 */
    private Integer totalPieces;

    /** 总数量（KG） */
    private BigDecimal totalQty;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
