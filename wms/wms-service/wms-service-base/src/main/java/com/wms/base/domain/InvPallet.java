package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 托盘信息
 */
@Data
@TableName("inv_pallet")
public class InvPallet extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 托盘号 */
    private String palletNo;

    /** 是否空托（1=空托 0=载货） */
    private Integer isEmpty;

    /** 当前仓位 */
    private String locCode;

    /** 托盘状态（闲/货） */
    private String status;

    /** 立库作业状态 */
    private String asrsStatus;

    /** 空托数量 */
    private Integer emptyQty;

    /** 在库箱数 */
    private Integer boxCount;

    /** 批号（同托同批） */
    private String batchNo;

    /** 总箱数 */
    private Integer totalBoxes;

    /** 总重量（KG） */
    private BigDecimal totalWeight;

    private String creator;

    private Date createTime;

    private String updater;

    private Date updateTime;
}
