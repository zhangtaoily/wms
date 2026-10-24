package com.wms.base.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wms.common.core.domain.IdEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 箱码信息（最小追溯单元）
 */
@Data
@TableName("inv_box")
public class InvBox extends IdEntity {
    private static final long serialVersionUID = 1L;

    /** 箱码 */
    private String boxCode;

    /** 批号 */
    private String batchNo;

    /** 缸号 */
    private String vatNo;

    /** 箱状态（待入/在库/已出库） */
    private String status;

    /** 仓库 */
    private String warehouseName;

    /** 仓位 */
    private String locCode;

    /** 所属托盘号 */
    private String palletNo;

    /** 入库时间 */
    private Date inTime;

    /** 出库时间 */
    private Date outTime;

    /** 筒数 */
    private Integer tubes;

    /** 标重（KG） */
    private BigDecimal stdWeight;

    /** 单筒重量（KG） */
    private BigDecimal tubeWeight;

    /** 毛重（KG） */
    private BigDecimal grossWeight;

    /** 净重（KG） */
    private BigDecimal netWeight;

    /** 待入重量（KG） */
    private BigDecimal waitWeight;

    /** 箱子总重（KG） */
    private BigDecimal boxTotal;

    /** 单件管理量 */
    private BigDecimal unitQty;

    /** 实际回潮（%） */
    private BigDecimal realMr;

    /** 公定回潮（%） */
    private BigDecimal stdMr;

    /** 质检组 */
    private String qcGroup;

    /** 检测时间 */
    private Date qcTime;

    private Date createTime;

    private Date updateTime;
}
