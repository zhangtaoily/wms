package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存批次仓位账（批次×仓库×仓位 在库量）
 */
@Data
@TableName("inv_batch_stock")
public class InvBatchStock extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 库存批次ID（inv_batch.id） */
    private Long invBatchId;

    /** 库存批号（冗余） */
    private String invBatchNo;

    /** 缸号 */
    private String vatNo;

    /** 仓库ID（base_warehouse.id） */
    private Long warehouseId;

    /** 仓库名称（冗余） */
    private String warehouseName;

    /** 仓位 */
    private String locCode;

    /** 件数（仓位在库） */
    private Integer pieces;

    /** 箱数 */
    private Integer boxes;

    /** 筒数 */
    private Integer tubes;

    /** 标重（KG） */
    private BigDecimal stdWeight;

    /** 毛重（KG） */
    private BigDecimal grossWeight;

    /** 净重（KG） */
    private BigDecimal netWeight;

    /** 原料成分（冗余） */
    private String comp;

    /** 色号 */
    private String colorCode;

    /** 色名 */
    private String colorName;

    /** 可用数量 */
    private BigDecimal allowQty;

    /** 同步状态 */
    private String syncStatus;

    private Date createTime;

    private Date updateTime;
}
